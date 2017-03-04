"""
--------------- Global Constants/Properties ---------------
"""
# Imports
from enum import Enum
from datetime import time

""" Enumeration Declarations """
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
    StatusCheck = 0
    NewMessage = 1

class EMessageKey(Enum):
    """
        Enum class used along with eNetMessageType as the keys
        for the NetMessage class dictionary
    """
    SourceId = 0
    MessageType = 1

    MessageGroupId = 2
    MessageContents = 3

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
    sDatabaseHost = 'localhost'
    sUserName = 'root'
    sDatabaseName = 'Lucerna'
    sSummaryTableName = 'ModeSummaryTable'
    sSettingsTableName = 'ModeSettingsTable'
    sAttrId = 'id'
    sAttrLblName = 'name'
    attrStartTime = 'start_time'
    sPayload = 'payload'

class NetConstants:
    iClientThreadLimit = 2
    iServerBasePort = 6000
    iServerReceiveBufferSize = 4096
    iServerBacklogConnections = 4
    iServerTimeout = 30
