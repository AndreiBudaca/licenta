import redis

module_name = "optimal-instances"

if __name__ == '__main__':
    r = redis.Redis(host='localhost', port=31381, decode_responses=True)
    r.lpush("traffic_interceptor_command", "delete_module;{0}".format(module_name))
    r.lpush("aggregator_config", "delete_module;{0}".format(module_name))
