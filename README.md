# OnlineStore
This is a REST API with JWT authentication.

### The application includes the following functionality:
- Users system: allows user to create an account and log in.
- Browse games: user can browse games available for purchase.
- Search: users can search games by title and tags.
- Library system: user's games are stored in the library. 
- Admin Panel: admin can add/edit/delete game.

### Database diagram visualization

![Image of database diagram visualization](docs/database.png)

Database used: H2.

----------------------------

### Endpoints information

| Endpoints                  | Method | Description                             | Request Headers | Request Parameters                     | Request Body                                                                                                                                                                                | Response Body                                            |
|----------------------------|--------|-----------------------------------------|-----------------|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| /api/auth/authenticate     | POST   | Authenticate user                       | None            | None                                   | {<br/>"email": "string", <br/>"password": "string"<br/>}                                                                                                                                    | {<br/>"status": "string",<br/> "message": "string"<br/>} |
| /api/auth/register         | POST   | Register user                           | None            | None                                   | {<br/>"email": "string", <br/>"password": "string"<br/>}                                                                                                                                    | {<br/>"status": "string",<br/> "message": "string"<br/>} |
| /api/catalogue/details     | GET    | Get details of a game                   | None            | gameId: Integer                        | None                                                                                                                                                                                        | list: Game                                               |
| /api/catalogue/gameManager | POST   | Add a new game to the store             | Authorization   | None                                   | {<br/>"id": 0,<br/>"title": "string",<br/>"description": "string",<br/>"releaseDate": "2023-04-20",<br/>"price": 0,<br/>"tags": [<br/>{<br/>"id": 0,<br/>"name": "string"<br/>}<br/>]<br/>} | message: string                                          |
| /api/catalogue/gameManager | DELETE | Remove a game from the store            | Authorization   | gameId: Integer                        | None                                                                                                                                                                                        | message: string                                          |
| /api/catalogue/gameManager | PUT    | Edit existing game                      | Authorization   | gameId: Integer                        | {<br/>"title": "string",<br/>"description": "string",<br/>"releaseDate": "2023-04-20",<br/>"price": 0,<br/>"tags": [<br/>{<br/>"id": 0,<br/>"name": "string"<br/>}<br/>]<br/>}              | message: string                                          |
| /api/catalogue/games       | GET    | Get all games from the store            | None            | None                                   | {<br/>"id": 0,<br/>"title": "string",<br/>"description": "string",<br/>"releaseDate": "2023-04-20",<br/>"price": 0,<br/>"tags": [<br/>{<br/>"id": 0,<br/>"name": "string"<br/>}<br/>]<br/>} | list: Game                                               |
| /api/catalogue/purchase    | POST   | Purchase the game                       | Authorization   | gameId: Integer                        | None                                                                                                                                                                                        | message: string                                          |
| /api/catalogue/search      | GET    | Search games by title and tags          | None            | title: string,<br/>tags:array[integer] | None                                                                                                                                                                                        | list: Game                                               |
| /api/library               | GET    | Get all games owned by a user           | Authorization   | None                                   | None                                                                                                                                                                                        | list: Game                                               |
| /api/library/details       | GET    | Get details of the game owned by a user | Authorization   | gameId: Integer                        | None                                                                                                                                                                                        | list: Game                                               |

Authorization requires a JSON Web Token (JWT).
