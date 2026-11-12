(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Client', Client)
        .factory('ClientByTypePersonne', ClientByTypePersonne)
        .factory('ClientByLot', ClientByLot);

    Client.$inject = ['$resource','BASE_URL_API'];
    ClientByTypePersonne.$inject = ['$resource','BASE_URL_API'];
    ClientByLot.$inject = ['$resource','BASE_URL_API'];

    function Client ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'clients/:id';

        return $resource(resourceUrl, {}, {
            'queryAll': { method: 'GET',url:BASE_URL_API+'clients', isArray: true},
            'query': { method: 'GET', isArray: true},
            'queryEntreprise': { method: 'GET',url:BASE_URL_API+'AllClientsEntrp' ,isArray: true},
            'queryParticulier': { method: 'GET',url:BASE_URL_API+'AllClients',isArray: true},
            'queryBytypePersonne': { method: 'GET',url:BASE_URL_API+'clients/type/:typePersonne', isArray: true},
            'queryBytypePer': { method: 'GET',url:BASE_URL_API+'clients/typeP', isArray: true},
            'findCompte': {methode: 'GET', url: BASE_URL_API+'clients/compte/:id', isArray: true},
            'getValeurCptClient': {method: 'GET', url: BASE_URL_API+'countCoupteAutre', isArray: false},
            'getComptePrincipal': {method: 'GET' ,url: BASE_URL_API+'returnComptePrincipalOfClient', isArray: false},
            'getValeurCompteOfClient': {method: 'GET', url: BASE_URL_API +'getValeurCompteOfClient', isArray: false},
            'deleteCompteBanqueOfClient': {method: 'DELETE', url: BASE_URL_API+'remove-compte-banque-of-client',isArray: false},
            // -------------------- new ---------------------------------------------------------------------------
            'allClients': { method: 'GET',url:BASE_URL_API+'clients-alls', isArray: true},
            'queryByClientTelephone': { method: 'GET', url: BASE_URL_API+'clients/filtrer/:telephone', isArray: true},
            'clientById':{method: "GET", url:BASE_URL_API+'client/:id', isArray:false},
            // ----------------------------------------------------------------------------------------------------
            'findOpListeClient':{method: 'GET',url: BASE_URL_API+'clients/type-optimise',isArray:true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
    function ClientByTypePersonne ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'clients/type';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'changeStatut': { method: 'GET',url:BASE_URL_API+'clients/changeStatut/:id', isArray: false},
            'identifier': { method: 'GET',url:BASE_URL_API+'clients/identifier/:id', isArray: false},
            'setProfileActeur': { method: 'GET',url:BASE_URL_API+'clients/:id/setProfileActeur/:profileId', isArray: false},
            'countClient':{method: 'GET', url: BASE_URL_API+'client-count', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return angular.fromJson({
                        typePersonne:data
                    });
                }
            },
            'update': { method:'PUT' }
        });
    }

    function ClientByLot ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'clients/filtrer/:telephone';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByClientTelephone': { method: 'GET', url: BASE_URL_API+'clients/filtrer/:telephone', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return angular.fromJson({
                        typePersonne:data
                    });
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
