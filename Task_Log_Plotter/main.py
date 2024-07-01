import logs
import plots
import redis


if __name__ == '__main__':
    r = redis.Redis(host='localhost', port=31381, decode_responses=True)
    logs.LogCollector.startCollecting(r)

    while True:
        print("Enter the graphs you want to be displayed (Ex: 2 4 5 for selecting the 2, 4 and 5 graph) or 0 to exit")
        for opt in plots.LogPlot.plots_menu:
            print(opt)

        userInput = input()
        try:
            opts = userInput.split(" ")
            opts = [int(x) for x in opts]

            if 0 in opts:
                break

            plots.LogPlot.showData(opts)
        except:
            break

    logs.LogCollector.stopCollecting()
