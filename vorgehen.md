- aus anforderungen: 1. remote-zugriff muss möglich sein -> server-client-struktur -> rest 2. daten sollten persistiert werden und zusätzlich nutzermanagement und interaktion zwischen den nutzern -> schließt cookies und filesystemlösung aus -> erfordert db 3. ->techstack: keine anforderungen aus der größe des projektes -> microservice-architektur unnötig, keine besonderen anforderungen an frontend - allgmein ähnlich zu banksystem, aber keine gehobenen ansprüche an sicherheit (kleine beträge, im intranet deployed?!) -> bedenken für anfälligkeit können außen vor gelassen werden (crsf,xss), jwt option (sicher genug ohne zusätzliches refresh token mit kurzem refresh timer und reduziert queries). gleichzeitiges verändern eines kontos mgl -> locking sollte bedacht werden (auch hier niedrigere prio wegen anzahl der nutzer) -> wenn dann optimistic locking -> acid safety sicher gestellt durch hibernate/mariadb</br></br>
  (Balance updates are already safe. sanction() calls addToAccountBalance, which does an atomic UPDATE ... SET balance = balance + :amount in SQL (PhraseUserRepository.java:16-18). That's a single statement executed by InnoDB, so concurrent sanctions against the same user can't lose an update — no explicit locking needed.)

            # -> keine gesonderten anforderungen -> spring (security/db-driver/hibernate(hauptsächlich locking und dirty checking))/react; docker (bzw. podman) bietet sich trotzdem an: insbesondere weil "echte" statt h2 db und leichtere migration in prod für wenig mehraufwand

    <ol type="a">
      <li>er diagramm erstellen</li>
      <li>lightweight docker images erstellen</li>
          - deswegen: alpine und mariaDB(alpine:maria existiert nicht mehr)
      <li>db schemas erstellen und bevölkern</li>
          - indexierung für 10 letzten strafen/likes unique constraint (implizit auch phrase_id)-> häufig benutzt. andere werden über fk implizit erstellt</br>
          - strafen mit types: 
          <ul>
          <li>soll nur 3 strafen geben</li>
          2 optionen: 
              <ol>
              <li>strafbetrag in db: vorteile für deploy (muss nicht neu deployed werden, wenn sich strafe ändert)</li>
              <li> strafbetrag im backend: schneller und einfacher</li>
              </ol>
          </li>
          <li>(optional) admin aktionen als type: macht admin aktionen nachvollziehbar/zeichnet history der aktionen auf</li>
          </ul> 
      <li>versionsverwaltung</li>
      <ul>
          <li>git (vertautheit)</li>
          <li>ermöglicht entwicklungsstufen (rückkehr/auschecken/abnahme: gewählt: 
          <ul>
          <li>branches für einzelne features (z.b.: sanctioning, account-reset usw. + tags (0.x.y wobei x für stufe steht und y für feature)</li>
          <li>neue feature branches zweigen von vorhergehenden ab -> spätere features enthalten alle vorhergehenden (stufen implizieren sukzessiven aufbau)</li>
          </ul>
          </li>
          <li>üblichen vorteile vcs: sichere featureentwicklung, zusammenarbeit und ci/cd (letzten 2 hier theoretisch)</li>
          <li>zu fortschrittspunkten:</li>
              <ul>
                  <li>straftypen direkt hinzugefügt, da es sich direkt aus dem schema ergab</li>
                  <li>gleiches für admin</li>
                  <li>0.2.1 akzeptanz: ich logge mich als admin ein und gelange nicht auf die phrase user seite, sondern auf eine seite, die mir admin aktionen ermöglicht</li>
            </ul>
          <li>.gitignore ignoriert .env/*log/docker overrides</li>
          <li>.dockerignore ignoriert ide-ordner/node-modules(wichtig, da ansonsten der auf windows compilierte ordner den für das image compilierten überschreibt</li>
      </ul>
    </ol>
    <li>backend programmieren:</li>
        - dependencies (hibernate, security, mariaDB, lombok (qol), testcontainer )</li>
        - tests mit testcontainer
    <li>frontend programmieren:</li>
        - wenig frameworks (redux, atkquery unnötig für aufgabe)
    <li>probleme:</li>
        <ul>
            <li>nutzer löschung is cascading (alle phrasen dieses nutzers werden gelöscht)</li>
            <li>microservice-überlegungen: </li>
                <ul>
                    <li>podman im moment dev-image -> sollte multistage build (mvn package → Fat-Jar → java -jar auf JRE-Base-Image) sein </li>
                    <li>ddl-auto: update: keine versionierte nachvollziehbare migrationen -> flyway/liquibase</li>
                    <li>kein health check (/actuator endpoint fehlt</li>
                    <li>pws in klartext in application.yaml</li>
                </ul>
        </ul>
    <li>verbesserungen:</li>
        <ul>
            <li>konto balance nirgendwo sichtbar</li>
            <li>nur nutzer mit guthaben werden im reset dropdown angezeigt</li>
            <li>admin kann liken</li>
            <li>eigene strafe liken</li>            
        </ul>

