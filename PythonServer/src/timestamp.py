from datetime import datetime, timedelta
from logger import *

class Timestamp:
    @staticmethod
    def _getTimeFormat():
        return '%Y-%m-%d %H:%M:%S.%f'

    @staticmethod
    def _formatTime(time):
        """
            convert datetime object into the standard formatted timestamp string
        """
        strTime = "0000-00-00 00:00:00.000000"
        if time is not None:
            strTime = "{:02d}-{:02d}-{:02d} {:02d}:{:02d}:{:02d}.{:06d}".format(
                time.year, time.month, time.day, time.hour,
                time.minute, time.second, time.microsecond)
        return strTime

    @staticmethod
    def _getRawTimeFromString(strTime):
        return datetime.strptime(strTime, Timestamp._getTimeFormat()).time()

    @staticmethod
    def getRawTime():
        return datetime.now()

    @staticmethod
    def getTimeDifferenceInSeconds(firstTime, secondTime):
        """
            Function to calculate the time difference between the current system time
            and some specified reference time. If the difference is positive, it indicates
            that the reference time is in the past. The exact value is specified as HH:MM:SS.
            If the return value is negative, the value specified is NOT the exact difference
            between the system time and reference time.
        """
        formattedFirstTime = Timestamp._formatTime(firstTime)
        formattedSecondtime = Timestamp._formatTime(secondTime)
        strpFirstTime = Timestamp._getRawTimeFromString(formattedFirstTime)
        strpSecondTime = Timestamp._getRawTimeFromString(formattedSecondtime)
        timeDiff = strpFirstTime - strpSecondTime
        log(DEBUG, "{}({}) - {}({}) = {}".format(strpFirstTime, formattedFirstTime, strpSecondTime, formattedSecondtime, timeDiff.total_seconds()))
        return timeDiff.total_seconds()
        