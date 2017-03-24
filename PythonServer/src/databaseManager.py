import MySQLdb
from constants import *
from logger import *
from timestamp import Timestamp
import itertools

class DatabaseManager:
    """
        Class responsible for sending updates to and receiving information from the MySQL database
        storing all of the group, user information. Also stores the individual threads.
    """
    def __init__(self):
        log(DEBUG, ">>")
        try:
            self.database = MySQLdb.connect(host=DatabaseManagerConstants.sDatabaseHost,
                                            user=DatabaseManagerConstants.sUsername,
                                            passwd=DatabaseManagerConstants.sPw,
                                            db=DatabaseManagerConstants.sDatabaseName)
        except Exception as e:
            log(ERROR, "Unable to connect to database! reason: {}".format(e))
        log(DEBUG, "<<")

    def _constructParameters(self, listParams):
        """
            Function to convert listParams to a format that is understandable by the mysql server.
            Function returns the formatted string of arguments.
        """
        sParams = ""
        bValid = (listParams is not None)

        if bValid:
            sParams = "("
            for currentArg in listParams:
                sParams += str(currentArg)
                sParams += ","

            sParams = sParams[:-1]
            sParams += ")"

        return sParams

    def _constructValues(self, listValues):
        """
            Function to convert listValues to a format that is understandable by the mysql server.
            Function returns the formatted string of arguments.
        """
        sValues = ""
        bValid = (listValues is not None)
        
        if bValid:
            sValues = "("
            for currentArg in listValues:
                if (type(currentArg) is str):
                    sValues += "'" + str(currentArg) + "'"
                else:
                    sValues += str(currentArg)
                sValues += ","

            sValues = sValues[:-1]
            sValues += ")"
            
        return sValues

    # def _constructConditions(self, listParams, listValues):
    #     """
    #         Function to construct the string containing the conditions such that the MySQL
    #         database can interpret the conditions.
    #     """
    #     return ""
    #     # TODO: implement this!

    def _executeQueryWithResponse(self, sQuery):
        """
            Function to execute the specified query and return the response as a string.
            Returns the response of the query as a string.
            
            params:
                sQuery - string denoting the query to execute.
        """
        # log(DEBUG, ">>")
        response = None
        try:
            cursor = self.database.cursor()
            cursor.execute(sQuery)
            response = cursor.fetchall()
            cursor.close()
            self.database.commit()
        except Exception as e:
            log(ERROR, "Unable to _executeQueries, reason: {}".format(e))
        # log(DEBUG, "<<")
        return list(itertools.chain(*response))

    def _constructQuery(self, eQueryType, sTableName, listParams, listValues = None, sCondition = "", bDistinct = False):
        """
            Function to convert the given parameters into a string that can be executed
            by a mysql server database.

            params:
                eQueryType - EQueryType enum that denotes the type of query

                sTableName - name of the table to perform the query on.

                listParams - list of string objects denoting the parameters.

                listValues - optional list of values corresponding to the parameters in the same order.
                    This is required depending on the query type.

                sCondition - optional condition used in the query
        """
        sQuery = ""
        bValid = True;

        sParams = self._constructParameters(listParams)
        sValues = self._constructValues(listValues)

        if eQueryType == EQueryType.Select:
            if bDistinct:
                sQuery = "SELECT DISTINCT {} FROM {}".format(sParams, sTableName)
            else:
                sQuery = "SELECT {} FROM {}".format(sParams, sTableName)
            if listValues is not None:
                log(ERROR, "listValues was found to not be none for select!")
                bValid = False

        elif eQueryType == EQueryType.Replace:
            sQuery = "REPLACE INTO {} {} VALUES {}".format(sTableName, sParams, sValues)
            if (sCondition is not ""):
                log(WARN, "Ignoring conditions ({}) specified in insert sQuery", sCondition)
                sCondition = ""

        elif eQueryType == EQueryType.Delete:
            sQuery = "DELETE FROM {}".format(sTableName)
            if ((sCondition is "") or (sCondition is None)):
                log(ERROR, "No conditions specified in delete statement!")
                bValid = False

        elif eQueryType == EQueryType.Count:
            sQuery = "SELECT COUNT(*) FROM {}".format(sTableName)

        else:
            log(ERROR, "Unknown query type {}".format(eQueryType))
            bValid = False

        if bValid:
            if (sCondition is not ""):
                sQuery += " WHERE {}".format(sCondition)
            log(DEBUG, "Contructed sQuery: {}".format(sQuery))
        else:
            log(ERROR, "Bad params")
            sQuery = ""
        return sQuery

    def addMessageToGroup(self, sUsername, sGroupName, sMessage, sTimestamp = None, iId = None):
        if iId is None:
            iId = self.getUserId(sUsername)
        if sTimestamp is None:
            sTimestamp = Timestamp._formatTime(Timestamp.getRawTime())

        if iId is not None:
            sReplaceQuery = self._constructQuery(EQueryType.Replace, DatabaseManagerConstants.sConversionsTableName,
                ("senderId","groupName","sentTime","status","message"), (iId, sGroupName, sTimestamp, EMessageStatus.Undelivered, sMessage))
            response = self._executeQueryWithResponse(sReplaceQuery)
        else:
            log(WARN, "User {} doesn't exist in {}".format(sUsername, DatabaseManagerConstants.sUserInfoTableName))

    # def updateMessageStatusForGroup(self, sGroupName):

    # def getMessagesForUser(self, sUsername):
    #     """
    #         Function to get al 
    #     """


    def getUserIdsInGroup(self, sGroupName):
        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserGroupTableName,
            ("id",),None,"groupName='{}'".format(sGroupName))
        response = self._executeQueryWithResponse(sSelectQuery)
        return response

    def getGroupListForUser(self, sUsername, iId = None):
        if iId is None:
            iId = self.getUserId(sUsername)

        response = None
        if iId is not None:
            sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserGroupTableName,
                ("groupName",),None,"id={}".format(iId))
            response = self._executeQueryWithResponse(sSelectQuery)
        else:
            log(WARN, "User {} doesn't exist in {}".format(sUsername, DatabaseManagerConstants.sUserInfoTableName))

        return response

    def getGroupList(self):
        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserGroupTableName,
            ("groupName",),None,"",True)
        response = self._executeQueryWithResponse(sSelectQuery)
        return response

    def getUserId(self, sUsername):
        """
            Function to return an integer representing the id of the specified username.
        """
        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserInfoTableName,
            ("id",), None, "name='{}'".format(sUsername))

        response = self._executeQueryWithResponse(sSelectQuery)

        bNoResponse = (response is None) or \
            ( (response is not None) and (not response) );

        iId = None
        if not bNoResponse:
            iId = int(response[0])
        return iId

    def joinGroup(self, sUsername, sGroupName, iId = None):
        """
            Function to add specified username to the specified groupName.
        """
        if iId is None:
            iId = self.getUserId(sUsername)

        if iId is not None:
            # Exit the group if already joined..
            self.exitGroup(sUsername, sGroupName, iId)

            sReplaceQuery = self._constructQuery(EQueryType.Replace, DatabaseManagerConstants.sUserGroupTableName,
                ("id","groupName"), (iId,sGroupName))
            self._executeQueryWithResponse(sReplaceQuery)
        else:
            log(WARN, "User {} doesn't exist in {}".format(sUsername, DatabaseManagerConstants.sUserInfoTableName))

    def exitGroup(self, sUsername, sGroupName, iId = None):
        """
            Function to exit specifed groupName.
        """
        if iId is None:
            iId = self.getUserId(sUsername)

        if iId is not None:
            sDeleteQuery = self._constructQuery(EQueryType.Delete, DatabaseManagerConstants.sUserGroupTableName,
                (), (), "id={} AND groupName='{}'".format(iId, sGroupName))
            self._executeQueryWithResponse(sDeleteQuery)
        else:
            log(WARN, "User {} doesn't exist in {}".format(sUsername, DatabaseManagerConstants.sUserInfoTableName))

    def insertUser(self, sUsername, sTimestamp = None):
        """
            Function to insert new user or update the timestamp for the said user to the
            specified timestamp. This function returns a boolean to indicate whether the
            insert was successful or not. The insert may not be successful if the parameters
            specified are incorrect.
        """
        iId = self.getUserId(sUsername)
        log(DEBUG, "checking if user exists: id={} name={}".format(iId, sUsername))

        if iId is None:
            # The current username does not exist!
            sCountQuery = self._constructQuery(EQueryType.Count, DatabaseManagerConstants.sUserInfoTableName, (), ())
            listCountResponse = self._executeQueryWithResponse(sCountQuery)

            iId = int( listCountResponse[0][0] )
            log(DEBUG, "{} does not exist in {}, creating new entry with id={}".format(
                sUsername, DatabaseManagerConstants.sUserInfoTableName, iId))

        if sTimestamp is not None:
            sTime = sTimestamp
        else:
            sTime = Timestamp._formatTime(Timestamp.getRawTime())
        sReplaceQuery = self._constructQuery(EQueryType.Replace, DatabaseManagerConstants.sUserInfoTableName,
            ("id", "name", "lastContactTime"), (iId, sUsername, sTime))
        self._executeQueryWithResponse(sReplaceQuery)


    ## CREATE FUNCTIONS FOR THESE BASIC COMMANDS TAKINGS VARIABLE INPUT PARAMETERS
    # CREATE TABLE UserInfo(id INT PRIMARY KEY, name VARCHAR(256), lastContactTime DATETIME);
    # CREATE TABLE UserGroup(id INT, groupName VARCHAR(256), FOREIGN KEY (id) REFERENCES UserInfo(id));
    # CREATE TABLE Conversations(senderId INT, groupName VARCHAR(256), sentTime DATETIME, status INT, message VARCHAR(256), FOREIGN KEY (senderId) REFERENCES UserInfo(id));

    # DELETE FROM UserGroup WHERE id=something AND groupName=something2

    # REPLACE INTO UserInfo(id, name, lastContactTime) VALUES (1, 'arup', '2016-03-02 22:50:21');
    # REPLACE INTO UserInfo(id, name, lastContactTime) VALUES (2, 'test', '2016-03-05 12:00:21');

    # INSERT INTO Conversations(senderId, groupName, sentTime, status, message) VALUES (1, 'asdf', '2016-03-05 12:00:21', 0, 'i love michael');

    # SELECT * FROM UserInfo;
    # SELECT COUNT(*) FROM UserInfo;
