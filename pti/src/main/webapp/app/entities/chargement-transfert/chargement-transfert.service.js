(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ChargementTransfert', ChargementTransfert);

    ChargementTransfert.$inject = ['$resource','BASE_URL_API'];

    function ChargementTransfert ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'chargement-transferts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getCompteOfLigneChg': {method: 'GET', url: BASE_URL_API+'findCompteByValeurCompte', isArray: false},
            'chargementByClient': {method: 'GET',url: BASE_URL_API+'findChargementClient/:id', isArray: true},
            'imprimer': { method: 'GET',url: BASE_URL_API+'chargement-transfert/etat/rapportChargement/:codeChargement',
                isArray: false ,responseType: 'arraybuffer' ,
                transformResponse: function (data, headers) {
                    var   response = {};
                    if (data) {
                        response.data = data;
                        response.headers = headers();
                        return response;
                    }
                    return data;
                }},

            'valider': { method: 'GET', url:BASE_URL_API+'chargement-transferts/valider/:id/:user/:canal', isArray: true},

            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },

            'imprimerSelonStatut': { method: 'GET',url: BASE_URL_API+'chargement-transfert/etat/rapportChargementSelonStatut/:codeChargement/:statut',
                isArray: false ,responseType: 'arraybuffer' ,
                transformResponse: function (data, headers) {
                    var   response = {};
                    if (data) {
                        response.data = data;
                        response.headers = headers();
                        return response;
                    }
                    return data;
                }},
        });
    }
})();
