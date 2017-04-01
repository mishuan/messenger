"""
--------------- Global Constants/Properties ---------------
"""
# Imports
from enum import Enum
from datetime import time

""" Enumeration Declarations """
class EQueryType(Enum):
    Select = 0
    Replace = 1
    Delete = 2
    Count = 3

class EMessageStatus(Enum):
    Undelivered = 0
    Delivered = 1

class ELoggerLevel(Enum):
    Debug = 0
    Warn = 1
    Info = 2
    Error = 3
    Pretty = 4

class EThreadType(Enum):
    All = 0
    ThreadedServer = 1
    ThreadedServerWorker = 2
    Database = 3

class EMessageType(Enum):
    ListGroups = 0
    JoinGroup = 1
    NewMessage = 2
    LeaveGroup = 3

class MessageKey:
    MessageType = "messageType"
    Username = "username"

    Message = "message"
    Timestamp = "timestamp"
    GroupName = "groupName"
    GroupList = "groupList"

""" Modified Data Structures """
class Dictionary(dict):
    def __missing__(self, key):
        return None

""" Class Constants """
class LoggerConstants:
    iDisplayedLogLevel = ELoggerLevel.Debug
    iMaxLoggedFiles = 10
    sLogBasePath = "./Logs"
    sLogFilePrefix = 'Messenger_'
    sLogFileSuffix = '.log'
    logFile = None
    bLogFileInitialized = False
    sLogPath = ""

class ThreadManagerConstants:
    iClientThreads = 1
    iServerThreadsBase = 1
    iServerThreadsSatellite = 1

class DatabaseManagerConstants:
    sDatabaseName = "ece416database"
    sUserInfoTableName = "UserInfo"
    sUserGroupTableName = "UserGroup"
    sConversionsTableName = "Conversations"
    sDatabaseHost = 'localhost'
    sUsername = 'root'
    sPw = '204lester'

class NetConstants:
    iClientThreadLimit = 2
    iServerBasePort = 6000
    iServerReceiveBufferSize = 4096
    iServerBacklogConnections = 4
    iServerTimeout = 30
