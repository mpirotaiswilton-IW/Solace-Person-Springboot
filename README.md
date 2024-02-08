# Solace-Person-SpringBoot
 A set of microservices built with Event-Driven Architecture and communicating with a Solace event broker

## Summary

[Solace-Person-SpringBoot](#solace-person-springboot)
* [Summary](#summary)
* [Setup and Pre-requisites](#setup-and-pre-requisites)
* [Running the Microservice](#running-the-microservice)
    * [Cleaning up Exited Containers](#cleaning-up-exited-containers)
* [Verifying our solution](#verifying-our-solution)
    * [Viewing our Database using pgAdmin](#viewing-our-database-using-pgadmin)

## Setup and Pre-requisites

1. If not already installed:

- Install the latest version of OpenJDK 17 on your device (The following page has a complete catalogue of OpenJDK downloads: [https://www.openlogic.com/openjdk-downloads](https://www.openlogic.com/openjdk-downloads))
- Install Docker on your device (you can use the following link for a guide: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/))

>If you are using Docker Desktop for Windows, make sure to use version **4.26.1** or lower. 
>
>The solution will not deploy with `docker-compose` if you are using version **4.27.1**. This is a known issue with this version of Docker Desktop for Windows.

2. Clone this repository or download the .zip file from GitHub (extract the downloaded zip file )

## Running the Microservice

1. Using a Command Line Interface of your choosing, change directory to the downloaded/cloned repository


2. To build both of our microservice, you will need to build a .jar file for each:  
    1. Build the Publisher
        
        Change directory to 
        `<Path to repo>/Solace-Person-Springboot/person_publisher`
        and run the command below.

        ```
        <# Linux/MacOs #>
        ./mvnw clean package

        <# Windows #>
        .\mvnw clean package
        ```

    2. Build the Subscriber
    
        Change directory to 
        `<Path to repo>/Solace-Person-Springboot/person_subscriber`
        then run the same command as in the previous step.

3. Once both builds are successful, run this command to deploy them along with an event broker:

    ```
    docker-compose up -d --build
    ```

4. 6 docker containers should now be running:
    * `publisher-microservice`: where a spring-boot api image, built using a Dockerfile, is containerized. This container is responsible for sending events contaning personal information to the event broker.
    * `subscriber-microservice`: where a spring-boot api image, built using a Dockerfile, is containerized. This container is responsible for consuming events contaning personal information from the event broker to store them as entries into a database.
    * `db`: where a Postgres database is containerized and used by the `person-subscriber` application.
    * `pgadmin`: where a pgAdmin container is used to access the containerized Postgres database.
    * `solace`: where a Solace event broker is containerized.
    * `solace-init`: where a python script runs to set up our `solace` container with all the queues and subscribed topics needed for our microservices to communicate.

5. After a few minutes, `publisher-microservice` will have ran and exited, sending events to be consumed by `subscriber-microservice` and populated our database. You can verify the `publisher-microservice` container has exited by running the following command:
    ```
    docker ps -f "name=publisher-microservice"
    ```

#### Cleaning up Exited Containers

At this point, there are 2 container which are no longer running: `solace-init` and `publisher-microservice`. These containers have ran to set-up our Solace event-broker and to send a batch of 1000 new people entries as events. To remove these exited containers, run the following command in a seperate CLI window: 
```
docker container prune -f
```
You can verify that 4 containers are running by using the following command in your CLI:
```
docker ps
```

## Verifying our Solution

When we deployed our microservices using docker-compose, `publisher-microservice` automatically sent 1000 entries of a mock data set (from the file [Mock_data.csv](https://github.com/mpirotaiswilton-IW/Solace-Person-Springboot/blob/main/person_publisher/src/main/resources/Mock_data.csv)) as events when the `solace-init` container had exited. From there, `subscriber-microservice` will be listening to those events, receiving the data and storing it into the `db` database. To verify our solution worked as intended, we will view the contents of the database.

### Viewing our Database using pgAdmin

Using a web browser of your choosing, head to <http://localhost:5050/>. You should see the pgAdmin login page. To verify both databases, make sure to sign in using the following credentials:

* Email : `admin@admin.com`
* Password : `admin`

You can change these credentials under the following `pgadmin` container environment variables in the "[docker-compose.yaml](https://github.com/mpirotaiswilton-IW/Solace-Person-Springboot/blob/main/docker-compose.yaml)" file: 

* `PGADMIN_DEFAULT_EMAIL`
* `PGADMIN_DEFAULT_PASSWORD`

After successfully logging into pgAdmin, click on `Add New Server` on the Dashboard Home Page:
1. In the General tab, name your server as you see fit
2. Navigate to the Connection tab
3. For the `host name/address`, use the name of the Postgres container `db`
4. Make sure the port field is `5432`
5. the `Username` field is defined by the `POSTGRES_USER` environment variable for the `db` container in the `docker-compose.yaml` file
6. the `Password` field is defined by the `POSTGRES_PASSWORD` environment variable for the `db` container in the `docker-compose.yaml` file
7. Click save and, in the Object explorer, under Servers you should see your newly saved server `db`. This is the database `subscriber-microservice` uses.
8. In the Object Explorer, under `db > Databases` select `admin`
9. In pgAdmin menu bar, select `Tools > Query Tool`. This will load a new window with a blank page to write an SQL query to our database.
10. Run the following query in the query tool: 
    ```
    SELECT * FROM people_table
    ```
11. This query should return exactly 1000 rows, you can verify that the list is accurate to the source data by comparing the result of the query to the csv file [Mock_data.csv](https://github.com/mpirotaiswilton-IW/Solace-Person-Springboot/blob/main/person_publisher/src/main/resources/Mock_data.csv)
