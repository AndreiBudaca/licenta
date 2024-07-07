import redis

requests = 500
pattern = 0
p1 = 50
p2 = 0
p3 = 0

if __name__ == '__main__':
    r = redis.Redis(host='localhost', port=31381, decode_responses=True)
    r.lpush("traffic_interceptor_command", "job;{0};{1};{2};{3};{4}".format(requests, pattern, p1, p2, p3))