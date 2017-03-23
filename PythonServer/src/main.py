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


    # TODO: CONVERT THE RECEIVED DICT OBJECT INTO DICTIONARY
    if isinstance(message, dict):
        message = Dictionary(message)

    if isinstance(message, Dictionary):
        messageType = message[MessageKey.MessageType]
        if messageType is not None:

            #TODO: Convert messageType into integer
            if messageType == EMessageType.ListGroups:
                username = message[MessageKey.MessageType]
                log(INFO, "Contacted by user {}")

                if username is not None:
                    databaseManager.insertUser(username);
                else:
                    log(ERROR, "username is None!")

            else:
                log(ERROR, "Unknown MessageType {}".format(messageType))
        else:
            log(ERROR, "No MessageType specified!! {}".format(message))
    else:
        log(ERROR, "The message is not a dictionary object!!".format(message))


    # This function echoes the message back to the client.
    if threadedServer is not None:
        threadedServer.replyMessage(connection, message)
    else:
        log(ERROR, "threaded server is none!")

if __name__ == '__main__':
    sDeviceName = socket.gethostname()
    threadManager = ThreadManager()
    threadedServer = initializeThreadedServer(receivedMessage, threadManager)
    databaseManager = DatabaseManager()


    # def insertUser(self, sUserName, timestamp = None):
    # databaseManager.insertUser("derp");
    # databaseManager.insertUser("a3roy");
    # databaseManager.insertUser("shyuan");
    # time.sleep(2);
    # databaseManager.insertUser("shyuan");
    # time.sleep(3);
    # databaseManager.insertUser("a3roy");

    while True:
        time.sleep(5)