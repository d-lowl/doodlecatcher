function generate_request(_model) {
    model = {}
    console.log(_model)
    clearView();
    $.getJSON("/api/verify",{
        lat: _model.lat,
        lon: _model.lon,
        time: _model.time,
        hash: _model.hash
    })
    .done(function(data){
        console.log(data)
        model = data
        updateView()
    })
    .always(updateView())
}

function clearView() {
    $("table").hide();
    $("#btn-signature, #btn-pic").attr("disabled", true);
}

function updateView() {
    $("table").hide();
    $("#btn-signature, #btn-pic").attr("disabled", true);
    if(model.verified) {
        $("table").show();
        $("#btn-signature, #btn-pic").removeAttr("disabled");
        $("#difficulty > :nth-child(2)").text(model.difficulty);
        $("#hash > :nth-child(2)").text(model.hash);
        $("#location > :nth-child(2)").text("("+model.lat+","+model.lon+")");
        $("#picture > :nth-child(2) > img").attr('src','/temp/'+model.seed+'.png');
        $("#btn-pic").attr('href','/temp/'+model.seed+'.png')
        $("#seed > :nth-child(2)").text(model.seed);
        $("#time > :nth-child(2)").text(model.time);
    }
}


$("#btn-verify").click(function() {
    generate_request(getModel($("#signature").val()))
//    generate_request();
})

$(function() {
    clearView();
})