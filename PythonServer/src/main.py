"""
    main.py
"""
import socket
from constants import *
from logger import *
from threadManager import ThreadManager
from threadedServer import ThreadedServer
from databaseManager import DatabaseManager
import time

threadedServer = None
databaseManager = None

def initializeThreadedServer(callback, threadManager):
    threadedServer = ThreadedServer(threadManager, ('', NetConstants.iServerBasePort), callback)
    if (threadedServer.isReady()):
        threadManager.launchNewThread(EThreadType.ThreadedServer, threadedServer.acceptConnections, ())
    else:
        log(ERROR,"threadedServer isn't ready to launch!")
    return threadedServer

def receivedMessage(sSourceIpAddress, connection, message):
    log(INFO, "{} sent {}".format(sSourceIpAddress, message))

    response = Dictionary()

    if isinstance(message, dict):
        message = Dictionary(message)

    if isinstance(message, Dictionary):
        messageType = int(message[MessageKey.MessageType])
        sUsername = str(message[MessageKey.Username])


        if message[MessageKey.MessageType] is not None and messageType is not None:
            if messageType == EMessageType.ListGroups:
                log(INFO, "Contacted by user {}".format(sUsername))

                if sUsername is not None:
                    databaseManager.insertUser(sUsername);
                else:
                    log(ERROR, "sUsername is None!")

                response[MessageKey.MessageType] = str(EMessageType.ListGroups)
                response[MessageKey.GroupList] = databaseManager.getGroupList()

            elif messageType == EMessageType.JoinGroup:
                sGroupName = message[MessageKey.GroupName]
                databaseManager.joinGroup(sUsername, sGroupName)

                response[MessageKey.MessageType] = str(EMessageType.JoinGroup)
                response[MessageKey.GroupName] = sGroupName

            elif messageType == EMessageType.LeaveGroup:
                sGroupName = message[MessageKey.GroupName]
                databaseManager.LeaveGroup(sUsername, sGroupName)

                response[MessageKey.MessageType] = str(EMessageType.ListGroups)
                response[MessageKey.GroupList] = databaseManager.getGroupList()

            elif messageType == EMessageType.NewMessage:
                sGroupName = message[MessageKey.GroupName]
                sMessage = message[MessageKey.Message]

                # if sMessage is none, the client is just requesting new msgs
                if sMessage is not None:
                    # insert the message if its not None
                    databaseManager.insertMessage(sUsername, sGroupName, sMessage)

                dictNewMessages = databaseManager.getMessagesForUser(sUsername)
                response[MessageKey.MessageType] = str(EMessageType.NewMessage)
                response[MessageKey.Message] = dictNewMessages

            else:
                log(ERROR, "Unknown MessageType {}".format(messageType))
        else:
            log(ERROR, "No/invalid MessageType specified!! raw={}/parsed={}".format(message, messageType))
    else:
        log(ERROR, "The message is not a dictionary object!!".format(message))

    # This function echoes the message back to the client.
    if threadedServer is not None:
        threadedServer.replyMessage(connection, response)
    else:
        log(ERROR, "threaded server is none!")

if __name__ == '__main__':
    sDeviceName = socket.gethostname()
    threadManager = ThreadManager()
    threadedServer = initializeThreadedServer(receivedMessage, threadManager)
    databaseManager = DatabaseManager()

    # databaseManager.insertUser("derp");
    # databaseManager.insertUser("a3roy");
    # databaseManager.insertUser("shyuan");

    # databaseManager.joinGroup("a3roy","group1");
    # databaseManager.joinGroup("a3roy","group2");
    # databaseManager.joinGroup("a3roy","group3");
    # databaseManager.joinGroup("shyuan","group1");
    # databaseManager.joinGroup("shyuan","group2");
    # databaseManager.joinGroup("shyuan","group3");

    # databaseManager.exitGroup("shyuan","group3");


    # log(INFO,"group1 {}".format(databaseManager.getUsersInGroup("group1")))
    # log(INFO,"group3 {}".format(databaseManager.getUsersInGroup("group3")))
    # log(INFO,"getGroupList {}".format(databaseManager.getGroupList()))
    # log(INFO,"a3roy {}".format(databaseManager.getGroupListForUser("a3roy")))
    # log(INFO,"shyuan {}".format(databaseManager.getGroupListForUser("shyuan")))

    # databaseManager.insertMessage("a3roy", "group1", "hello world!")
    # databaseManager.insertMessage("shyuan", "group1", "phew!")

    # log(INFO, "msgs={}".format(databaseManager.getMessagesForUser("a3roy")))

    while True:
        time.sleep(5)

