"""
    main.py
"""
import socket
from constants import *
from logger import *
from threadManager import ThreadManager
from threadedServer import ThreadedServer
import time

threadedServer = None

def initializeThreadedServer(callback, threadManager):
    threadedServer = ThreadedServer(threadManager, ('', NetConstants.iServerBasePort), callback)
    if (threadedServer.isReady()):
        threadManager.launchNewThread(EThreadType.ThreadedServer, threadedServer.acceptConnections, ())
    else:
        log(ERROR,"threadedServer isn't ready to launch!")
    return threadedServer

def receivedMessage(connection, message):
    log(INFO, "receivedMessage = {}".format(message))

    # This function echoes the message back to the client.
    if threadedServer is not None:
        threadedServer.replyMessage(connection, message)
    else:
        log(ERROR, "threaded server is none!")

if __name__ == '__main__':
    sDeviceName = socket.gethostname()
    threadManager = ThreadManager()
    threadedServer = initializeThreadedServer(receivedMessage, threadManager)
    while True:
        time.sleep(5)