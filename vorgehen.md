- aus anforderungen: 1. remote-zugriff muss möglich sein -> server-client-struktur -> rest 2. daten sollten persistent sein und zusätzlich nutzermanagement und interaktion zwischen den nutzern -> schließt cookies und filesystemlösung aus -> erfordert db 3. ->techstack: keine anforderungen aus der größe des projektes -> indexierung unnötig, microservice-architektur unnötig, keine besonderen anforderungen an frontend - allgmein ähnlich zu banksystem, aber keine gehobenen ansprüche an sicherheit (kleine beträge) -> bedenken für anfälligkeit können außen vor gelassen werden (crsf,xss), jwt option (overhead-reduzierung könnte relevant sein). gleichzeitiges verändern eines kontos mgl -> locking sollte bedacht werden (auch hier niedrigere prio wegen anzahl der nutzer und höhe der beträge) -> wenn dann optimistic locking</br>

          # -> keine gesonderten anforderungen -> spring (security/db-driver/hibernate(hauptsächlich locking und dirty checking))/react; docker (bzw. podman) bietet sich trotzdem an: insbesondere weil "echte" statt h2 db und leichtere migration in prod für wenig mehraufwand

  <ol type="a">
    <li>er diagramm erstellen</li>
    <li>lightweight docker images erstellen</li>
        - deswegen: alpine und mariaDB(alpine:maria existiert nicht mehr)
    <li>db schemas erstellen und bevölkern</li>
        - indexierung für 10 letzten strafen (häufig benutzt)</br>
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
    <li>backend programmieren:</li>
        - dependencies (hibernate, security, mariaDB, lombok (qol) )</li>
    <li>frontend programmieren:</li>
        - wenig frameworks (redux, atkquery unnötig für aufgabe)
    <li>verbesserungen:</li>
        <ul>
            <li>nur nutzer mit guthaben werden im reset dropdown angezeigt</li>
        </ul>
  </ol>

