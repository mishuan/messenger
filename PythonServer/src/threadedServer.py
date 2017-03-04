import socket
import json
import threading
from logger import *
from constants import NetConstants, Dictionary, EThreadType

class ThreadedServer:
    """
        Class responsible for handling multiple server connections.
        Is able to launch new threads via ThreadManager to handle new connections.
        Its constructor should be launched in a new thread as the accept function is dedicated to one thread
    """

    def __init__(self, threadManager, serverAddress, callbackFunction):
        """
            Constructor 
            params:
                threadManager: reference to ThreadManager object
                serverAddress: tuple (ipAddr, port); ipAddr ignored, port used to for server 
                callbackFunction: function to call when messages are received
                    (note any extra args required for the call back should be saved as class objects)
        """
        log(DEBUG, '>> {}'.format(serverAddress))

        self.threadManager = threadManager
        self.serverAddress = serverAddress
        self.callbackFunction = callbackFunction
        self.connection = None
        self.clients = Dictionary()
        self.bReadyToAccept = False

        try:
            # Initialize reusable socket
            self.serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.serverSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            try:
                self.serverSocket.bind(self.serverAddress)
                self.serverSocket.listen(NetConstants.iServerBacklogConnections)
                self.bReadyToAccept = True
            except Exception as e:
                log(ERROR, "Unable to bind/listen socket {0}, reason: {1}".format(self.serverAddress, e))
                self.closeSocket()
        except Exception as e:
            log(ERROR, "Unable to instantiate server socket, reason: {0}".format(e))
        log(DEBUG, '<<')


    def __del__(self):
        """
            Destructor
        """
        log(DEBUG, '>>')
        try:
            if (self.connection is not None):
                self.connection.close()
        except Exception as e:
            log(WARN, "Unable to close connection..")
        try:
            if (self.serverSocket is not None):
                self.closeSocket()
        except Exception as e:
            log(WARN, "Unable to close connection..")
        self.serverSocket = None
        self.serverAddress = None
        log(DEBUG, '<<')

    def isReady(self):
        return self.bReadyToAccept

    def acceptConnections(self):
        """
            Main function that the ThreadedServer runs in.
            Accepts new connections and handles them in separate thread.
        """
        log(DEBUG, '>>')
        while True:
            try:
                connection, clientAddress = self.serverSocket.accept()
                connection.settimeout(NetConstants.iServerTimeout);
                self.threadManager.launchNewThread(EThreadType.ThreadedServerWorker, self.receiveMessage, (connection,))
            except Exception as e:
                log(ERROR, "Unable to accept new client, reason: {0}".format(e))
                self.closeSocket()
                break
            except KeyboardInterrupt:
                self.closeSocket()
                break
        log(DEBUG, '<<')

    def replyMessage(self, connection, message):
        """
            Function to reply to a TCP client given an existing connection and
            a Dictionary-formatted message. This function converts the message
            to a JSON serialized object and sends it back using the connection.

            Params:
                connection - Connection reference from receiveMessage function

                message - Dictionary-formatted object storing the message
        """
        try:
            serializedData = json.dumps(message)
            connection.send(serializedData)
        except Exception as e:
            log(ERROR, "Unable to send message, reason: {}".format(e))
            log(ERROR, "Msg: {}".format(message))


    def receiveMessage(self, connection):
        """
            Function to receive message and invoke callback function
            params:
                connection: connection object established by some client
        """
        log(DEBUG, '>>')
        while True:
            try:
                serializedData = connection.recv(NetConstants.iServerReceiveBufferSize)
                log(DEBUG, '>>1 {}'.format(serializedData))
                if serializedData:
                    message = json.loads(serializedData) # decode msg                     
                    log(DEBUG, '>>2 {}'.format(message))
                    self.callbackFunction(connection, message)
                    
                else:
                    raise Exception('Client disconnected')
            except Exception as e:
                connection.close()
                log(DEBUG, 'Done Receiving Message: {}'.format(e))
                log(DEBUG, '<<')
                return

    def closeSocket(self):
        log(DEBUG, '>>')
        try:
            self.serverSocket.shutdown(socket.SHUT_RDWR)
            self.serverSocket.close()
        except:
            log(DEBUG, "Unable to properly close socket..")
        log(DEBUG, '<<');
