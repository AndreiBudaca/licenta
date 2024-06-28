from matplotlib import pyplot as plt
from matplotlib.animation import FuncAnimation

import logs


class TaskDelayPlot:
    def __init__(self, graph, ax):
        self.graph = graph
        self.ax = ax

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["traffic_interceptor"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(1, len(data))]
        y_data = [data[i].timestamp - data[i - 1].timestamp for i in range(1, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.graph.set_xdata(x)
        self.graph.set_ydata(y)

        if len(x) > 0:
            self.ax.set_xlim(x[0], x[-1])
            self.ax.set_ylim(min(y) * 0.8, max(y) * 1.2)


class ReplicaPlot:
    def __init__(self, graph, ax):
        self.graph = graph
        self.ax = ax

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["task_dispatcher_replicas"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(0, len(data))]
        y_data = [data[i].replicas for i in range(0, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.graph.set_xdata(x)
        self.graph.set_ydata(y)

        if len(x) > 0:
            self.ax.set_xlim(x[0], x[-1])
            self.ax.set_ylim(min(y) * 0.8, max(y) * 1.2)


class WaitingQueuePlot:
    def __init__(self, graph, ax):
        self.graph = graph
        self.ax = ax

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["task_dispatcher_messages"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(0, len(data))]
        y_data = [data[i].queueLength for i in range(0, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.graph.set_xdata(x)
        self.graph.set_ydata(y)

        if len(x) > 0:
            self.ax.set_xlim(x[0], x[-1])
            self.ax.set_ylim(min(y) * 0.8, max(y) * 1.2)


class PartialResponses:
    def __init__(self, graph, ax):
        self.graph = graph
        self.ax = ax

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["aggregator_partial_responses"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(0, len(data))]
        y_data = [(1 if data[i].verdict == "Normal" else 0) for i in range(0, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.graph.set_xdata(x)
        self.graph.set_ydata(y)

        if len(x) > 0:
            self.ax.set_xlim(x[0], x[-1])
            self.ax.set_ylim(min(y) * 0.8, max(y) * 1.2)


class FinalResponses:
    def __init__(self, graph, ax):
        self.graph = graph
        self.ax = ax

    @staticmethod
    def get_data():
        data = logs.LogCollector.logs["aggregator_final_responses"].getData()
        x_data = [(data[i].timestamp - logs.LogCollector.timestamp) / 1000 for i in range(0, len(data))]
        y_data = [(1 if data[i].verdict == "Normal" else 0) for i in range(0, len(data))]

        return x_data, y_data

    def update(self):
        x, y = self.get_data()
        self.graph.set_xdata(x)
        self.graph.set_ydata(y)

        if len(x) > 0:
            self.ax.set_xlim(x[0], x[-1])
            self.ax.set_ylim(min(y) * 0.8, max(y) * 1.2)


class LogPlot:
    objects = None
    plots_sources = {
        1: TaskDelayPlot.get_data,
        2: ReplicaPlot.get_data,
        3: WaitingQueuePlot.get_data,
        4: PartialResponses.get_data,
        5: FinalResponses.get_data,
    }
    plots_objects = {
        1: lambda graph, ax: TaskDelayPlot(graph, ax),
        2: lambda graph, ax: ReplicaPlot(graph, ax),
        3: lambda graph, ax: WaitingQueuePlot(graph, ax),
        4: lambda graph, ax: PartialResponses(graph, ax),
        5: lambda graph, ax: FinalResponses(graph, ax),
    }

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
            graph = ax[i].plot(x, y)[0]

            LogPlot.objects.append(LogPlot.plots_objects[options[i]](graph, ax[i]))

        anim = FuncAnimation(fig, LogPlot.update, frames=None)
        plt.show()
