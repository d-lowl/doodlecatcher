function generate_request() {
    model = {};
    clearView();
    getLocation(function(position){
        updateModel(position,function(){
            updateView()
        })
    })
}

function clearView() {
    $("#picture, #seed, #time").hide();
    $("#btn-signature, #btn-pic").attr("disabled", true);
    $("#difficulty > :nth-child(2)").text("");
    $("#hash > :nth-child(2)").text("");
    $("#location > :nth-child(2)").text("");
    $("#caught > :nth-child(2)").text("");
}

function updateView() {
    $("#picture, #seed, #time").hide();
    $("#btn-signature, #btn-pic").attr("disabled", true);
    $("#difficulty > :nth-child(2)").text(model.difficulty);
    $("#hash > :nth-child(2)").text(model.hash);
    $("#location > :nth-child(2)").text("("+model.lat+","+model.lon+")");
    $("#caught > :nth-child(2)").text((model.isCaught ? "Yes" : "No"));

    if(model.isCaught) {
        $("#picture, #seed, #time").show();
        $("#btn-signature, #btn-pic").removeAttr("disabled");
        $("#picture > :nth-child(2) > img").attr('src','/temp/'+model.seed+'.png');
        $("#btn-pic").attr('href','/temp/'+model.seed+'.png')
        $("#seed > :nth-child(2)").text(model.seed);
        $("#time > :nth-child(2)").text(model.time);
    }
}

function updateModel(position,callback) {
    model = {}
    $.getJSON("/api/generate",{
        lat: Math.round(position.coords.latitude),
        lon: Math.round(position.coords.longitude)
    })
    .done(function(data){
        model = collect(model, data)
    })
    .error(function(err){
        console.log(err)
    })
    .always(callback)
}

function getLocation(callback) {
    callback({coords: {latitude: 0, longitude: 0}})

//    if (navigator.geolocation) {
//        navigator.geolocation.getCurrentPosition(callback,function(err) {
//            callback({coords: {latitude: 0, longitude: 0}})
//        });
//    } else {
//        callback({coords: {latitude: 0, longitude: 0}})
//    }
}

new Clipboard('#btn-signature', {
    text: getSignature
})

$("#btn-again").click(function() {
    generate_request();
})

$(function() {
    generate_request();
});