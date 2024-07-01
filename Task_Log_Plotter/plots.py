import math

from matplotlib import pyplot as plt
from matplotlib.animation import FuncAnimation

import logs


class TaskDelayPlot:
    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Task Delay")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Delay(ms)')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["traffic_interceptor"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(1, len(data))]
        y_data = [data[i].timestamp - data[i - 1].timestamp for i in range(1, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        self.ax.plot(x, y)
        self.ax.set_title("Task Delay")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Delay(ms)')


class ReplicaPlot:
    faasNames = []

    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Replicas")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Replica count')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["task_dispatcher_replicas"].getData()
        FaasResults.faasNames = []
        faasData = {
            "x": [],
            "y": []
        }

        for result in data:
            if result.faasName in FaasResults.faasNames:
                i = FaasResults.faasNames.index(result.faasName)
            else:
                FaasResults.faasNames.append(result.faasName)
                faasData["x"].append([])
                faasData["y"].append([])
                i = len(FaasResults.faasNames) - 1

            faasData["x"][i].append((result.timestamp - logs.LogCollector.timestamp) / 1000)
            faasData["y"][i].append(result.replicas)

        return faasData["x"], faasData["y"]

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        if len(x) > 0 and hasattr(x[0], "__len__"):
            for j in range(len(x)):
                self.ax.plot(x[j], y[j], label=FaasResults.faasNames[j])
            self.ax.legend(loc='upper left')
        else:
            self.ax.plot(x, y)

        self.ax.set_title("Replicas")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Replica count')


class WaitingQueuePlot:
    faasNames = []

    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Waiting queue length")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Message count')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["task_dispatcher_messages"].getData()
        FaasResults.faasNames = []
        faasData = {
            "x": [],
            "y": []
        }

        for result in data:
            if result.faasName in FaasResults.faasNames:
                i = FaasResults.faasNames.index(result.faasName)
            else:
                FaasResults.faasNames.append(result.faasName)
                faasData["x"].append([])
                faasData["y"].append([])
                i = len(FaasResults.faasNames) - 1

            faasData["x"][i].append((result.timestamp - logs.LogCollector.timestamp) / 1000)
            faasData["y"][i].append(result.queueLength)

        return faasData["x"], faasData["y"]

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        if len(x) > 0 and hasattr(x[0], "__len__"):
            for j in range(len(x)):
                self.ax.plot(x[j], y[j], label=FaasResults.faasNames[j])
            self.ax.legend(loc='upper left')
        else:
            self.ax.plot(x, y)

        self.ax.set_title("Waiting queue length")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Message count')


class PartialResponses:
    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Partial responses")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Normal(1) or Hostile(0)')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["aggregator_partial_responses"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(0, len(data))]
        y_data = [(1 if data[i].verdict == "Normal" else 0) for i in range(0, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        self.ax.plot(x, y)
        self.ax.set_title("Partial responses")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Normal(1) or Hostile(0)')


class FinalResponses:
    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Final responses")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Normal(1) or Hostile(0)')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["aggregator_final_responses"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(0, len(data))]
        y_data = [(1 if data[i].verdict == "Normal" else 0) for i in range(0, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        self.ax.plot(x, y)
        self.ax.set_title("Final responses")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Normal(1) or Hostile(0)')


class FaasResults:
    faasNames = []

    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Trust scores")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Trust score (%)')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["faas_result"].getData()
        FaasResults.faasNames = []
        faasData = {
            "x": [],
            "y": []
        }

        for result in data:
            if result.faasName in FaasResults.faasNames:
                i = FaasResults.faasNames.index(result.faasName)
            else:
                FaasResults.faasNames.append(result.faasName)
                faasData["x"].append([])
                faasData["y"].append([])
                i = len(FaasResults.faasNames) - 1

            faasData["x"][i].append((result.timestamp - logs.LogCollector.timestamp) / 1000)
            faasData["y"][i].append(result.result)

        return faasData["x"], faasData["y"]

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        if len(x) > 0 and hasattr(x[0], "__len__"):
            for j in range(len(x)):
                self.ax.plot(x[j], y[j], label=FaasResults.faasNames[j])
            self.ax.legend(loc='upper left')
        else:
            self.ax.plot(x, y)

        self.ax.set_title("Trust scores")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Trust score (%)')


class FaasProcessingTime:
    faasNames = []

    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Processing time")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Time(ms)')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["faas_processing_time"].getData()
        FaasResults.faasNames = []
        faasData = {
            "x": [],
            "y": []
        }

        for result in data:
            if result.faasName in FaasResults.faasNames:
                i = FaasResults.faasNames.index(result.faasName)
            else:
                FaasResults.faasNames.append(result.faasName)
                faasData["x"].append([])
                faasData["y"].append([])
                i = len(FaasResults.faasNames) - 1

            faasData["x"][i].append((result.timestamp - logs.LogCollector.timestamp) / 1000)
            faasData["y"][i].append(result.processingTime)

        return faasData["x"], faasData["y"]

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        if len(x) > 0 and hasattr(x[0], "__len__"):
            for j in range(len(x)):
                self.ax.plot(x[j], y[j], label=FaasResults.faasNames[j])
            self.ax.legend(loc='upper left')
        else:
            self.ax.plot(x, y)

        self.ax.set_title("Processing time")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Time(ms)')


class AggregatorWeights:
    faasNames = []

    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Aggregator weights")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Weight')

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["aggregator_weights"].getData()
        FaasResults.faasNames = ["sum"]
        faasData = {
            "x": [[]],
            "y": [[]]
        }

        for result in data:
            sum = 0
            for key in result.weights:
                if key in FaasResults.faasNames:
                    i = FaasResults.faasNames.index(key)
                else:
                    FaasResults.faasNames.append(key)
                    faasData["x"].append([])
                    faasData["y"].append([])
                    i = len(FaasResults.faasNames) - 1

                faasData["x"][i].append((result.timestamp - logs.LogCollector.timestamp) / 1000)
                faasData["y"][i].append(result.weights[key])
                sum += math.pow(math.e, (-result.weights[key]))

            faasData["x"][0].append((result.timestamp - logs.LogCollector.timestamp) / 1000)
            faasData["y"][0].append(sum)

        return faasData["x"], faasData["y"]

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        if len(x) > 0 and hasattr(x[0], "__len__"):
            for j in range(len(x)):
                self.ax.plot(x[j], y[j], label=FaasResults.faasNames[j])
            self.ax.legend(loc='upper left')
        else:
            self.ax.plot(x, y)

        self.ax.set_title("Aggregator weights")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Weight')


class TotalLatency:
    def __init__(self, ax):
        self.ax = ax
        ax.set_title("Total latency")
        ax.set_xlabel('Time(s)')
        ax.set_ylabel('Time(ms)')

    @staticmethod
    def get_data():
        data1 = logs.LogCollector.logs["aggregator_partial_responses"].getData()
        data2 = logs.LogCollector.logs["aggregator_final_responses"].getData()

        x_data = [[], []]
        y_data = [[], []]

        x_data[0] = [(el.timestamp - logs.LogCollector.timestamp) / 1000 for el in data1]
        y_data[0] = [el.decisionTime for el in data1]

        x_data[1] = [(el.timestamp - logs.LogCollector.timestamp) / 1000 for el in data2]
        y_data[1] = [el.decisionTime for el in data2]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.ax.clear()

        self.ax.plot(x[0], y[0], label="Partial results")
        self.ax.plot(x[1], y[1], label="Final results")
        self.ax.legend(loc='upper left')

        self.ax.set_title("Total latency")
        self.ax.set_xlabel('Time(s)')
        self.ax.set_ylabel('Time(ms)')


class LogPlot:
    objects = None
    plots_sources = {
        1: TaskDelayPlot.get_data,
        2: ReplicaPlot.get_data,
        3: WaitingQueuePlot.get_data,
        4: PartialResponses.get_data,
        5: FinalResponses.get_data,
        6: FaasResults.get_data,
        7: FaasProcessingTime.get_data,
        8: AggregatorWeights.get_data,
        9: TotalLatency.get_data,
    }
    plots_objects = {
        1: lambda graph: TaskDelayPlot(graph),
        2: lambda graph: ReplicaPlot(graph),
        3: lambda graph: WaitingQueuePlot(graph),
        4: lambda graph: PartialResponses(graph),
        5: lambda graph: FinalResponses(graph),
        6: lambda graph: FaasResults(graph),
        7: lambda graph: FaasProcessingTime(graph),
        8: lambda graph: AggregatorWeights(graph),
        9: lambda graph: TotalLatency(graph),
    }
    plots_menu = [
        "1. Task Delay Plot",
        "2. Replica Plot",
        "3. Waiting Queue Length Plot",
        "4. Partial Responses Plot",
        "5. Final Responses Plot",
        "6. Module Results Plot",
        "7. Module Processing Times Plot",
        "8. Module Weights Plot",
        "9. Total Latency Plot",
    ]

    @staticmethod
    def update(figure):
        for obj in LogPlot.objects:
            obj.update()

    @staticmethod
    def showData(options):
        fig, ax = plt.subplots(len(options))
        fig.tight_layout(pad=1.0)

        LogPlot.objects = []
        for i in range(len(options)):
            x, y = LogPlot.plots_sources[options[i]]()
            if len(x) > 0 and hasattr(x[0], "__len__"):
                for j in range(len(x)):
                    ax[i].plot(x[j], y[j]) if len(options) > 1 else ax.plot(x[j], y[j])
            else:
                ax[i].plot(x, y) if len(options) > 1 else ax.plot(x, y)

            LogPlot.objects.append(LogPlot.plots_objects[options[i]](ax[i] if len(options) > 1 else ax))

        anim = FuncAnimation(fig, LogPlot.update, interval=200, frames=None)
        plt.show()
