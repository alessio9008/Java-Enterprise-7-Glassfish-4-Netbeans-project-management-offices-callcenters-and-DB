var globalCounter = 0;
var selections = [];

(function main()
{
    console.log("Main script is starting...");

    $("#viability-add-new").bind("click", function() {
        if(checkNotEquals())addActivity(false);
        else {
            alertRedWithButton("Two or more values are equals");
        }
    });

    addActivity(true);
    submit();
})();

function submit() {
    $("#submit").click(function() {
        alertBlue("Processamento in corso della tua richiesta...");
        if(checkNotEquals())AsyncCallJQuery();
        else{
            alertRedWithButton("Two or more values are equals");
        }
    });

}
function addActivity(start)
{
    if (globalCounter >= 5)
    {
        alertRedWithButton("You can add up to 5 elements!");
    }
    else if (start != undefined && start == false && globalCounter < 5)
    {
        var html = loadHTML("formFile.html");
        $("#viability-new").append(html);
        setID();
        globalCounter++;
    }
    else if (start != undefined && start == true)
    {
        var html = loadHTML("formFile.html");
        $("#viability-new").html(html);
        setID();
        globalCounter++;
    }
    else {
        console.error("error in addActivity");
    }
}

function setID()
{
    $("#selector-id-").attr("name", "selector-id-" + globalCounter);
    $("#selector-street-id-").attr("name", "selector-street-id-" + globalCounter);
    $("#status-id-").attr("name", "status-id-" + globalCounter);

    $("#selector-id-").attr("id", "selector-id-" + globalCounter);
    $("#selector-street-id-").attr("id", "selector-street-id-" + globalCounter);
    $("#status-id-").attr("id", "status-id-" + globalCounter);

    $("#form-container").attr("id", "form-container-" + globalCounter);

    $("#form-container-" + globalCounter).slideUp(0);
    $("#form-container-" + globalCounter).slideDown(1000);
}

function alertRedWithButton(message)
{
    $("#alert-div").empty();
    var theMessage = "Error";
    if (message != undefined && message != "")
        theMessage = message;

    var html = "<div class='alert alert-danger alert-dismissable alert-class'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>&times;</button><strong>Error! </strong>" + theMessage + "</div>";
    $("#alert-div").html(html);
}

function alertGreenWithButton(message)
{
    $("#alert-div").empty();
    var theMessage = "Error";
    if (message != undefined && message != "")
        theMessage = message;

    var html = "<div class='alert alert-success alert-dismissable alert-class'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>&times;</button>" + theMessage + "</div>";
    $("#alert-div").html(html);
}

function alertYellowWithButton(message)
{
    $("#alert-div").empty();
    var theMessage = "Error";
    if (message != undefined && message != "")
        theMessage = message;

    var html = "<div class='alert alert-warning alert-dismissable alert-class'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>&times;</button>" + theMessage + "</div>";
    $("#alert-div").html(html);
}

function alertBlue(message)
{
    $("#alert-div").empty();
    var theMessage = "Error";
    if (message != undefined && message != "")
        theMessage = message;

    var html = "<div class='alert alert-info'>" + theMessage + "</div>";
    $("#alert-div").html(html);
}

function loadHTML(url)
{
    var output;
    $.ajax(
            {
                url: url,
                async: false,
                success: function(data)
                {
                    output = data;
                },
                error: function()
                {
                    console.error("Errore nel caricamento del file " + url);
                }
            });
    return output;
}

function AsyncCall() {
    httpReq = new XMLHttpRequest();
    var url = "ServletClient1?";
    var params = "";
    for (i = 0; i < globalCounter; i++) {
        params = params + "selector-id-" + i + "=" + $("#selector-id-" + i + "").val() + "&selector-street-id-" + i + "=" + $("#selector-street-id-" + i + "").val() + "&status-id-" + i + "=" + $("#status-id-" + i + "").val() + "&";
    }
    params = params + "submit=submit";
    var res;
    url = url + params;
    httpReq.open("GET", url, true);

    httpReq.onreadystatechange = requestHandler;

    httpReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpReq.send(params);
}

function requestHandler() {//Call a function when the state changes.
    if (httpReq.readyState == 4 && httpReq.status == 200) {
        res = httpReq.responseText;
        controll(res);
    }
    //else alert("Attenzione!\n\nLa query non e' andata a buon fine: molto probabilmente hai lasciato campi vuoti (tutti i campi sono obbligatori) oppure hai inserito in alcuni di essi valori non validi.");
}

function controll(res) {
    if (res.toLowerCase().trim() == "abort")
        alertRedWithButton("<strong>The request has been aborted! </strong>");
    else if (res.toLowerCase().trim() == "commit")
        alertGreenWithButton("<strong>Well done! </strong>" + res);
    else
        alertYellowWithButton("<strong>Unexpected message! </strong>" + res);

}

function AsyncCallJQuery() {
    var url = "ServletClient1?";
    var params = "";
    for (i = 0; i < globalCounter; i++) {
        params = params + "selector-id-" + i + "=" + $("#selector-id-" + i + "").val() + "&selector-street-id-" + i + "=" + $("#selector-street-id-" + i + "").val() + "&status-id-" + i + "=" + $("#status-id-" + i + "").val() + "&";
    }
    params = params + "submit=submit";
    var res;
    url = url + params;
    $.get(url, function(res){
        controll(res);
    });
}

function checkNotEquals(){
    var flag=true;
    for(i=0;i<globalCounter;i++){
        values1=$("#selector-id-" + i + "").val();
        x=i-1;
        while(x>=0){
            values2=$("#selector-id-" + x + "").val();
            if(values1==values2) flag=false;
            x--;
        }
    }
    return flag;
}