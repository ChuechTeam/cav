#!/usr/bin/env python3

# run.py: launches services; like a poor man's Docker Compose
#
# USAGE:
# 
# Windows: python run.py [modules...]
# Linux:   ./run.py [modules...]
#
# Where modules is a list of modules you want to spawn, which can be either:
# - client
# - service
# - discovery
# - all: launches all modules!
#
# EXAMPLES:
#
# run.py client               # Boots up the client only
# run.py service client       # Boots up the service and the client
# run.py discovery service    # Boots up the discovery server and the client

import pathlib
import os
import subprocess
import sys
import shutil
import time

# Path stuff
root_path = pathlib.Path(__file__).absolute().parent

mvn_path = root_path / ('mvnw.cmd' if os.name == 'nt' else 'mvnw')
java_path = shutil.which("java")

if java_path is None:
    print("No java executable found! Please install Java 25, take a look at the README for some info.", file=sys.stderr)
    sys.exit(1)

client_jar_path = root_path / "client" / "target" / "client.jar"
service_jar_path = root_path / "service" / "target" / "service.jar"
discovery_jar_path = root_path / "discovery" / "target" / "discovery.jar"

# Parse the command line args
run_client = False
run_service = False
run_discovery = False
for arg in sys.argv:
    if arg == "client" or arg == "all": run_client = True
    if arg == "service" or arg == "all": run_service = True
    if arg == "discovery" or arg == "all": run_discovery = True

if not run_client and not run_service and not run_discovery:
    print("No modules to run! Usage: run.py [modules...]. Modules can be 'client', 'service', or 'discovery', or even 'all' if you feel like it!")
    sys.exit(1)

# First off... package things up
maven_result = subprocess.run([mvn_path, "-T", "1C", "package", "-DskipTests"], cwd=str(root_path))

if maven_result.returncode != 0:
    print("Failed to build the Maven project!", file=sys.stderr)
    sys.exit(1)
    
# Spawn the services
alive_procs = []
if run_discovery: alive_procs.append(("Discovery", subprocess.Popen([java_path, "-jar", discovery_jar_path], text=True)))
if run_service: alive_procs.append(("Service", subprocess.Popen([java_path, "-jar", service_jar_path], text=True)))
# here we use run to redirect stdin properly else it's a nightmare honestly
if run_client: subprocess.run([java_path, "-jar", client_jar_path], text=True)

# Wait for them to all die.
# TODO: neat key thing to see each process' output by pressing "1", or "2", etc. Won't support scrolling though
while len(alive_procs) > 0:
    try:
        time.sleep(0.25)
        
        for i in range(len(alive_procs) - 1, -1, -1):
            name, process = alive_procs[i] 
            return_code = process.poll()
            if return_code is not None:
                print("💀 Process " + name + " died! " + str(return_code))
                alive_procs.pop(i)
    except KeyboardInterrupt:
        for name, proc in alive_procs:
            proc.terminate()

sys.stdout.flush()
sys.stderr.flush()