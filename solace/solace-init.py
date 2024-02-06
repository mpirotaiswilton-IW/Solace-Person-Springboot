import requests
import time

from requests.auth import HTTPBasicAuth



class Subscription:
    def __init__(self, topic):
        self.topic = topic


class Queue:
    def __init__(self, name, access_type, maxMsgSpoolUsage, permission, ingress_enabled, egress_enabled, subscribed_topics):
        self.name = name
        self.access_type = access_type
        self.maxMsgSpoolUsage = maxMsgSpoolUsage
        self.permission = permission
        self.ingress_enabled = ingress_enabled
        self.egress_enabled = egress_enabled
        self.subscribed_topics = subscribed_topics

r = requests.Response()
r.status_code = 400

host_name = "solace"
msg_vpn_name = "default"

username = "admin"
password = "admin"

basic = HTTPBasicAuth(username, password)

queues = [
    Queue(name = "PeopleQ",
          access_type = "exclusive", 
          maxMsgSpoolUsage = 5000,
          permission = "consume", 
          ingress_enabled = True, 
          egress_enabled = True,
          subscribed_topics=
          [
              Subscription("people/add")
          ])
]

while r.status_code != 200 :
    
    print("Attempting connection to Solace Event Broker...")
    try:
        r = requests.get(url="http://" + host_name + ":8080/SEMP/v2/config/msgVpns/" + msg_vpn_name + "/queues", auth=basic)
    except: 
        print("Connection Failed, retrying...")
        time.sleep(1)

print("Connection successful!")

content_header = {'Content-Type': 'application/json'}

for queue in queues:

    print("Sending request to create queue: " + queue.name)

    print("\n -------- \n")

    queue_req_payload = {
        'queueName': queue.name,
        'permission': queue.permission,
        'accessType': queue.access_type,
        'maxMsgSpoolUsage': queue.maxMsgSpoolUsage,
        'ingressEnabled': queue.ingress_enabled,
        'egressEnabled': queue.egress_enabled
    }
    



    req_url = "http://" + host_name + ":8080/SEMP/v2/config/msgVpns/" + msg_vpn_name + "/queues"

    queue_req = requests.post(url=req_url, json = queue_req_payload, headers = content_header, auth=basic)

    print(queue_req.text)

    queue_check = requests.get(url="http://" + host_name + ":8080/SEMP/v2/config/msgVpns/" + msg_vpn_name + "/queues/" + queue.name, auth=basic)

    while(queue_check.status_code != 200):

        print(queue_check.status_code)

        queue_check = requests.get(url="http://" + host_name + ":8080/SEMP/v2/config/msgVpns/" + msg_vpn_name + "/queues/" + queue.name, auth = basic)
    
    print("\n -------- \n")
    print("Queue creation successful! : " + queue.name)

    for subscribed_topic in queue.subscribed_topics:

        print("Sending request to create subscribed topic: " + subscribed_topic.topic + " --- for queue: " + queue.name)


        topic_req_payload = {
        'subscriptionTopic': subscribed_topic.topic
        }
    
        req_url = "http://" + host_name + ":8080/SEMP/v2/config/msgVpns/" + msg_vpn_name + "/queues/" + queue.name + "/subscriptions"

        topic_req = requests.post(url=req_url, 
                                    json=topic_req_payload,
                                    headers= content_header,
                                    auth=basic)
        
        print(topic_req.text)
        
print("Queues and Topic creation finished")