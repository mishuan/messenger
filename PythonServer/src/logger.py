# import time
import inspect
import os
import glob
from datetime import datetime
from constants import ELoggerLevel, LoggerConstants

# LOG LEVELS
INFO = ELoggerLevel.Info
DEBUG = ELoggerLevel.Debug
WARN = ELoggerLevel.Warn
ERROR = ELoggerLevel.Error
PRETTY = ELoggerLevel.Pretty

def initializeLogger():
    log(DEBUG, ">>")
    openNewLogFile()
    deleteOldFiles()
    log(INFO, "<<")

def logLevelToString(level):
    ret = ""
    if (level == INFO): ret = 'INFO'
    if (level == DEBUG): ret = 'DEBUG'
    if (level == WARN): ret = 'WARN'
    if (level == ERROR): ret = 'ERROR'
    return ret

def openNewLogFile():
    log(DEBUG, ">>")
    sLogPath = LoggerConstants.sLogBasePath + '/' + LoggerConstants.sLogFilePrefix + getFileFormattedTime() + LoggerConstants.sLogFileSuffix
    log(DEBUG, "setting LoggerConstants.logfile location to {}".format(sLogPath))
    if not os.path.exists(LoggerConstants.sLogBasePath):
        os.makedirs(LoggerConstants.sLogBasePath)
    try:
        LoggerConstants.sLogPath = sLogPath
        LoggerConstants.logFile = open(sLogPath, 'w')
        LoggerConstants.bLogFileInitialized = True
    except Exception as e:
        log(ERROR, "Unable to open log file {}".format(sLogPath))
    log(DEBUG, "<<")

def deleteOldFiles():
    count = 0
    log(DEBUG, ">>")
    listFiles = os.listdir(LoggerConstants.sLogBasePath)
    listFiles.reverse()
    for file in listFiles:
        fullname = os.path.join(LoggerConstants.sLogBasePath, file)
        count += 1
        if (count > LoggerConstants.iMaxLoggedFiles):
            try:
                os.remove(fullname)
                log(DEBUG, "removing old log {}".format(fullname))
            except Exception as e:
                log(ERROR, "Unable to delete file, reason: {}".format(e))
    log(DEBUG, "<<")

def getFileFormattedTime():
    now = datetime.now()
    return "{:04d}_{:02d}_{:02d}-{:02d}_{:02d}_{:02d}_{:06d}".format(now.year, now.month, now.day, now.hour, now.minute, now.second, now.microsecond)

def getTime():
    now = datetime.now()
    return "{:04d}-{:02d}-{:02d} {:02d}:{:02d}:{:02d}.{:06d}".format(now.year, now.month, now.day, now.hour, now.minute, now.second, now.microsecond)

def getRawTime():
    return datetime.now()

def getFileAndFunctionNames():
    callerFrame = inspect.getouterframes(inspect.currentframe(), 4)
    callerFile = callerFrame[2][1].split('/')[-1]
    callerLine = callerFrame[2][2]
    callerFunction = callerFrame[2][3]
    return(callerFile, callerLine, callerFunction)

def clearScreen():
    print(chr(27) + "[2J")

def log(level, msg):
    if (LoggerConstants.iDisplayedLogLevel <= level):
        (callerFile, callerLine, callerFunction) = getFileAndFunctionNames()
        if (level == PRETTY):
            prettyMsg = "{}".format(msg)
        else:
            prettyMsg = "[{}][{}::{}({:03d})]  {}:  {}".format(getTime(), callerFile, callerFunction, callerLine, logLevelToString(level), msg)

        print prettyMsg
        if LoggerConstants.bLogFileInitialized:
            LoggerConstants.logFile.write(prettyMsg)
            LoggerConstants.logFile.write('\n')
