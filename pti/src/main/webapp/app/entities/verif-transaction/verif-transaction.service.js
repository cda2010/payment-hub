(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('VerifTransaction', VerifTransaction);

    VerifTransaction.$inject = ['$resource','BASE_URL_API'];

    function VerifTransaction ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'';

        return $resource(resourceUrl, {}, {
            'queryByParamProduit': {
                method: 'POST',
                url: resourceUrl+"mobile/omnnicanal/get_data",
                isArray: true,
                transformRequest: function (data, headersGetter) {
                    var idProduit = data.idProduit;
                    var mparametre = data.mparametre.map(function(parametre) {
                        return {
                            codeParametre: parametre.codeParametre,
                            valeur: parametre.valeur,
                            affiche: parametre.affiche
                        };
                    });
                    var requestData = {
                        idProduit: idProduit,
                        mparametre: mparametre
                    };
                    return angular.toJson(requestData);
                }
            },
      
        });
    }
})();
