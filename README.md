# CMU

## Servidor:
#### endpoints:
- /register?name=joao&passwdHashBase64=123
 -(registar novo utilizador joao com pass 123 (exemplo, pass deve ser hash))
- /login?name=joao&passwdHashBase64=123                               (fazer login, devolve token q deve ser posteriormente utilizado)
- /createAlbum?name=pedro&token=ObVNGg==&album=test                   (criar novo album, de notar q o token varia conforme o login)
- /postLink?name=pedro&token=ObVNGg==&album=test&link=https://...     (adicionar novo link ao album test)
- /retrive?name=pedro&token=ObVNGg==&album=test                       (saber q links estao no album test, devolve json igual a {"name":"test","links":["https://..."]})
- /addClient2Album?name=pedro&token=ObVNGg==&album=test&client2Add=joao (adiciona outro client ao album test)
