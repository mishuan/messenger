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

    if isinstance(message, dict):
        message = Dictionary(message)

    if isinstance(message, Dictionary):
        messageType = int(message[MessageKey.MessageType])
        username = message[MessageKey.Username]

        if message[MessageKey.MessageType] is not None and messageType is not None:

            if messageType == EMessageType.ListGroups:
                log(INFO, "Contacted by user {}".format(username))

                if username is not None:
                    databaseManager.insertUser(username);
                else:
                    log(ERROR, "username is None!")

                # TODO: respond with list of groups

            elif messageType == EMessageType.JoinGroup:
                databaseManager.JoinGroup();

            else:
                log(ERROR, "Unknown MessageType {}".format(messageType))
        else:
            log(ERROR, "No/invalid MessageType specified!! raw={}/parsed={}".format(message, messageType))
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

    while True:
        time.sleep(5)