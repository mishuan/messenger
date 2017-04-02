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

    def _constructParameters(self, listParams, bIsSelectQuery = False):
        """
            Function to convert listParams to a format that is understandable by the mysql server.
            Function returns the formatted string of arguments.
        """
        sParams = ""
        bValid = (listParams is not None)

        if bValid:
            if not bIsSelectQuery:
                sParams = "("
            for currentArg in listParams:
                sParams += str(currentArg)
                sParams += ","

            sParams = sParams[:-1]
            if not bIsSelectQuery:
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
                if isinstance(currentArg, str):
                    sValues += "'" + str(currentArg) + "'"
                else:
                    sValues += str(currentArg)
                sValues += ","

            sValues = sValues[:-1]
            sValues += ")"
            
        return sValues

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

    def _executeQueryWithRawResponse(self, sQuery):
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
        return response

    def _constructQuery(self, eQueryType, sTableName, listParams, listValues = None, sCondition = "", bDistinct = False):
        """
            Function to convert the given parameters into a string that can be executed
            by a mysql server database.

            params:
                eQueryType - EQueryType enum that denotes the type of query

                sTableName - username of the table to perform the query on.

                listParams - list of string objects denoting the parameters.

                listValues - optional list of values corresponding to the parameters in the same order.
                    This is required depending on the query type.

                sCondition - optional condition used in the query
        """
        sQuery = ""
        bValid = True;

        sParams = self._constructParameters(listParams, (eQueryType == EQueryType.Select))
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

    def isUserInDatabase(self, sUsername):
        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserInfoTableName,
            ("username",),None,"username='{}'".format(sUsername))
        response = self._executeQueryWithResponse(sSelectQuery)

        bResult = False
        if response:
            bResult = True
        log(ERROR, "response = {} {}".format(response, bResult))

        return bResult


    def getUsersInGroup(self, sGroupName):
        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserGroupTableName,
            ("username",),None,"groupName='{}'".format(sGroupName))
        response = self._executeQueryWithResponse(sSelectQuery)
        return response

    def getGroupListForUser(self, sUsername):
        response = None

        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserGroupTableName,
            ("groupName",),None,"username='{}'".format(sUsername))
        response = self._executeQueryWithResponse(sSelectQuery)

        log(INFO, "{} {}".format(response, isinstance(response, list)))

        return response

    def getGroupList(self):
        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserGroupTableName,
            ("groupName",),None,"",True)
        response = self._executeQueryWithResponse(sSelectQuery)
        return response

    def getLastContactTime(self, sUsername):
        sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sUserInfoTableName,
            ("lastContactTime",), None, "username='{}'".format(sUsername) )
        response = self._executeQueryWithResponse(sSelectQuery)

        log(INFO, "{} - {}".format(response, response[0]))

        return response[0]
        # return Timestamp._formatTime(response[0])

    def joinGroup(self, sUsername, sGroupName):
        """
            Function to add specified username to the specified groupName.
        """
        # Exit the group if already joined..
        self.exitGroup(sUsername, sGroupName)

        sReplaceQuery = self._constructQuery(EQueryType.Replace, DatabaseManagerConstants.sUserGroupTableName,
            ("username","groupName"), (sUsername,sGroupName))
        self._executeQueryWithResponse(sReplaceQuery)

    def exitGroup(self, sUsername, sGroupName):
        """
            Function to exit specifed groupName.
        """
        sDeleteQuery = self._constructQuery(EQueryType.Delete, DatabaseManagerConstants.sUserGroupTableName,
            (), (), "username='{}' AND groupName='{}'".format(sUsername, sGroupName))
        self._executeQueryWithResponse(sDeleteQuery)

    def insertUser(self, sUsername, sTimestamp = None):
        """
            Function to insert new user or update the timestamp for the said user to the
            specified timestamp. This function returns a boolean to indicate whether the
            insert was successful or not. The insert may not be successful if the parameters
            specified are incorrect.
        """
        log(DEBUG, "checking if user exists: username={}".format(sUsername))
        # Delete the user from the list first
        sDeleteQuery = self._constructQuery(EQueryType.Delete, DatabaseManagerConstants.sUserInfoTableName,
            None, None, "username='{}'".format(sUsername))
        self._executeQueryWithResponse(sDeleteQuery)

        if sTimestamp is not None:
            sTime = sTimestamp
        else:
            sTime = Timestamp._formatTime(Timestamp.getRawTime())
        sReplaceQuery = self._constructQuery(EQueryType.Replace, DatabaseManagerConstants.sUserInfoTableName,
            ("username", "lastContactTime"), (sUsername, sTime))
        self._executeQueryWithResponse(sReplaceQuery)

    def getMessagesForUser(self, sUsername):
        groupList = self.getGroupListForUser(sUsername)
        dictNewMessages = Dictionary()

        sLastContactTime = self.getLastContactTime(sUsername)

        for sGroupName in groupList:
            sSelectQuery = self._constructQuery(EQueryType.Select, DatabaseManagerConstants.sConversionsTableName,
                ("username", "groupName", "sentTime", "message"), None, "groupName='{}' AND sentTime >= '{}'".format(sGroupName, sLastContactTime))

            # sSelectQuery += " ORDER BY convert(sentTime, decimal)"
            sSelectQuery += " ORDER BY sentTime"

            listOutputMessagesForGroup = list()
            listMessagesForGroup = self._executeQueryWithRawResponse(sSelectQuery)
            for sUsername, sGroupName, sTimestamp, sMessage in listMessagesForGroup:
                # sTimestampInSeconds = Timestamp.getNumberOfSecondsFromString(sTimestamp)
                listOutputMessagesForGroup.append((sUsername,sGroupName,sTimestamp,sMessage))
            dictNewMessages[sGroupName] = listOutputMessagesForGroup

        return dictNewMessages

    def insertMessage(self, sUsername, sGroupName, sMessage):
        sReplaceQuery = self._constructQuery(EQueryType.Replace, DatabaseManagerConstants.sConversionsTableName,
            ("username", "groupName", "sentTime", "message"),
            (sUsername,sGroupName,Timestamp._formatTime(Timestamp.getRawTime()),sMessage),"")

        self._executeQueryWithResponse(sReplaceQuery)

    ## CREATE FUNCTIONS FOR THESE BASIC COMMANDS TAKINGS VARIABLE INPUT PARAMETERS
    # CREATE TABLE UserInfo(username VARCHAR(256), lastContactTime VARCHAR(256));
    # CREATE TABLE UserGroup(groupName VARCHAR(256), username VARCHAR(256));
    # CREATE TABLE Conversations(username VARCHAR(256), groupName VARCHAR(256), sentTime VARCHAR(256), message VARCHAR(256));

    # DELETE FROM UserGroup WHERE id=something AND groupName=something2

    # REPLACE INTO UserInfo(username, lastContactTime) VALUES ('arup', '2016-03-02 22:50:21');
    # REPLACE INTO UserInfo(username, lastContactTime) VALUES ('test', '2016-03-05 12:00:21');

    # INSERT INTO Conversations(username, groupName, sentTime, message) VALUES ('derp', asdf', '2016-03-05 12:00:21.000000', 'i love michael');

    # SELECT * From Conversations ORDER BY convert(sentTime, decimal);
    # SELECT * FROM UserInfo;
    # SELECT COUNT(*) FROM UserInfo;
