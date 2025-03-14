docker load -i todo-app.jar
docker run -p 8080:4242 --name todo-app-container -e VERBOSE=1 -d todo-app