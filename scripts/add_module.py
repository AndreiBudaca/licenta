import redis

module_name = "optimal-instances"
module_input_queue = "td-optimal-instances"
module_layers = "7"


if __name__ == '__main__':
    r = redis.Redis(host='localhost', port=31381, decode_responses=True)
    r.lpush("traffic_interceptor_command", "add_module;{0};{1};{2}".format(module_name, module_input_queue, module_layers))
    r.lpush("aggregator_config", "add_module;{0}".format(module_name))
