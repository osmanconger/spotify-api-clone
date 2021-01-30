This is a REST API for a music application like spotify that uses Spring Boot. It consists of two microservices: profiles and songs. Song microservice uses MongoDB and has endpoints for adding, 
deleting, updating and finding songs. Profile microservice uses Neo4j and has endpoints for creating a profile, liking/unliking songs, 
following/unfollowing friends, and getting the songs liked by friends. By using microservices that communicate with one another, it makes use of a document-oriented 
database and a graphical database at the same time. 
