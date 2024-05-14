import matplotlib.pyplot as plt


def read_data(filename):
    values = []
    with open(filename) as file:
        for line in file:
            line_vals = line.split(' ')
            x_val = int(line_vals[0])
            y_val = int(line_vals[1])
            values.append([x_val, y_val])

    return values


def plot_data(x_data, y_data):
    fig, ax = plt.subplots(3)
    for i in range(len(x_data)):
        ax[i].plot(x_data[i], y_data[i])
    plt.show()


if __name__ == '__main__':
    producer_log = read_data("logs/log_producer.txt")
    task_dispacher_logs = read_data("logs/log_task_dispacher.txt")
    consumer_log = read_data("logs/log_consumer.txt")

    log_x_data = [
        [x[0] for x in producer_log[1:]],
        [x[0] for x in task_dispacher_logs[1:]],
        [x[0] for x in consumer_log[1:]]
    ]

    log_y_data = [
        [x[1] for x in producer_log[1:]],
        [x[1] for x in task_dispacher_logs[1:]],
        [-x[1] for x in consumer_log[1:]]
    ]

    plot_data(log_x_data, log_y_data)
