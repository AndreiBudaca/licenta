import logs
import plots
import redis


if __name__ == '__main__':
    r = redis.Redis(host='localhost', port=31381, decode_responses=True)

    logs.LogCollector.startCollecting(r)
    plots.LogPlot.showData([4, 5])
