<h1>Финалити</h1>
<h3>Для запуска необходимо: </h3>
<ol>
<li> Прописать <code>127.0.0.1 keycloak</code>  в вашем <code>/etc/hosts</code> файле (Linux) или 
<code>C:\Windows\System32\drivers\etc\hosts</code> (Windows). 
Это необходимо для того, чтобы работал Authorization Code Flow (OAuth2), 
а именно чтобы браузер мог быть перенаправлен на url с доменом keykloak.</li>

<li> Перейти в директорию <code>./docker-config</code> запустить docker compose: <code>docker compose up</code> </li>

<li> Следует ждать пока создадутся докер-образы, и пока запустятся все необходимые контейнеры. Ожидание 
запуска контейнеров может составлять около минуты. Контейнер api-gateway может выбрасывать исключения и перезапускаться
несколько раз, пока контейнер keycloak не перейдет в полностью рабочее состояние.</li>
</ol>
<h3>Как получить jwt token?</h3>
<p>Зайти в браузер по url : <code>http://localhost:8081/login</code> или <code>http://localhost:8081/get-token</code>.
Произойдет перенаправление на сайт провайдера аутентификации (Keycloak), необходимо будет пройти аутентификацию. Затем произойдет
обратное перенаправление и на странице будет выведен jwt token, который следует использовать в дальнейшем в качестве Bearer token.
Время действия - 1 час.
Готовые данные для входа: <ol>
<li>
<p>
login: admin
<p>
password: admin
<p>
(Roles: admin)
</li>
<li>
<p>
login: js
<p>
password: js
<p>
(Roles: journalist, subscriber)
</li>
<li>
<p>
login: journ
<p>
password: journ
<p>
(Roles: journalist)
</li>
<li>
<p>
login: sub
<p>
password: sub
<p>
(Roles: subscriber)
</li>
</ol>
<h3>Endpoints info</h3>
<p><code>http://localhost:8081/news/swagger</code></p>
<p><code>http://localhost:8081/comments/swagger</code></p>

<h3>Как в целом все устроено</h3>
<p>Имеется единая точка входа в микросервисное приложение - Api Gateway. Микросервисы и Api Gateway 
используют Eureka Discovery Server для взаимного обнаружения. Кроме Api Gateway, Eureka, есть
News-service, Comments-service. News-service запрашивает комментарии и оповещает о создании, удалении новостей
с помощью Spring Openfeign. У этих двух микросервисов разные базы данных. В базе данных для News-service имеется
одна таблица, cодержащая всю информацию о новостях. В базе данных для Comments-service содержится две таблицы,
одна для обеспечения согласованности данных News_id, хранящая id новостей находящихся в базе данных
News-service и айди соответствующего автора новости. Другая таблица хранит всю информацию о комментариях и имеет
внешний ключ news_id, зависящий от id в таблице News_id. Добавлен кастомный кеш стартер. Имеются WireMock, MockMvc тесты.
Сервисы покрыты на 100%.</p>