var model = {}

function collect() {
  var ret = {};
  var len = arguments.length;
  for (var i=0; i<len; i++) {
    for (p in arguments[i]) {
      if (arguments[i].hasOwnProperty(p)) {
        ret[p] = arguments[i][p];
      }
    }
  }
  return ret;
}

function getModel(string) {
    return JSON.parse(atob(string))
}

function getSignature() {
    const _model = {
        lat: model.lat,
        lon: model.lon,
        time: model.time,
        hash: model.hash
    }
    return btoa(JSON.stringify(_model))
}