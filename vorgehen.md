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
    <li>git (vertautheit</li>
    <li>ermöglicht entwicklungsstufen (rückkehr/auschecken/abnahme</li>
    <li>üblichen vorteile vcs: sichere featureentwicklung, zusammenarbeit und ci/cd (letzten 2 hier theoretisch)</li>
  </ul>
  <li>backend programmieren:</li>
      - dependencies (hibernate, security, mariaDB, lombok (qol) )</li>
  </ol>
