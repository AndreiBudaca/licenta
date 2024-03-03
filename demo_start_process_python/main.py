import subprocess

result = subprocess\
    .run(["python", "D:\\Documents\\Facultate\\An 4\\licenta\\Coduri\\demo_start_process_python\\dummy.py"],
         stdout=subprocess.PIPE)\
    .stdout

print("Result:", result.decode())