import socket
import json
import threading
from logger import *

iThreadLimit = 16


class ThreadedClient:
    """
    """

    def __init__(self, threadManager):
        """
            Constructor
            params
                threadManager: reference to threadManager object
        """
        log(DEBUG, '>>')
        self.iThreadLimit = iThreadLimit
        self.threadManager = threadManager
        self.threadSem = threading.Semaphore(iThreadLimit)
        log(DEBUG, '<<')

    def __del__(self):
        """
            Destructor
        """
        log(DEBUG, '>>')
        self.iThreadLimit = None
        self.threadManager = None
        log(DEBUG, '<<')

    def sendMessage(self, dstAddress, message):
        """
            Function to open connection to a server and send specified message.
            params:
                dstAddress: tuple (ipAddr, port) denoting server to connect to
                message: (ENetMessageType, payload) tuple containing message to be sent
        """
        log(DEBUG, '>> sending to {} message={}'.format(dstAddress, message))
        self.threadSem.acquire(True) # Block here if thread limit is reached
        self.threadManager.launchNewThread(EThreadType.ThreadedClient, self._sendMessage, (dstAddress, deepcopy(message),))
        log(DEBUG, '<<')

    def _sendMessage(self, dstAddress, message):
        """
            Private function to open connection to a server and send specified message.
            Intented to run on its own thread.
            params:
                dstAddress: tuple (ipAddr, port) denoting server to connect to
                message: (ENetMessageType, payload) tuple containing message to be sent
                callbackFunction: Function to be called once the response from server is received
                    the only argument of this function is expected to be message (the same as above)
                    nothing is done if the callbackFunction is None
                callbackArgs: args to passed into the function in addition to any response from the server
        """
        log(DEBUG, '>>')
        clientSocket = self.connect(dstAddress)
        if (clientSocket is not None):
            try:
                serializedData = json.dumps(message)
                clientSocket.sendall(serializedData)
            except Exception as e:
                log(ERROR, "Unable to send message, reason: {}".format(e))
                log(ERROR, "Msg: {}".format(message))
            finally:
                self.close(dstAddress, clientSocket);
                self.threadSem.release()
        else:
            self.threadSem.release() # Not sure about the release here, if we launch from the thread to get here, we can assume the semaphore was just acquired...
        log(DEBUG, '<<')



