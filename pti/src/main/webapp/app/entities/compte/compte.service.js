(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Compte', Compte)
        .factory('ValeurCompte', ValeurCompte)
        .factory('CompteByUO', CompteByUO);

    Compte.$inject = ['$resource','BASE_URL_API'];
    ValeurCompte.$inject = ['$resource','BASE_URL_API'];
    CompteByUO.$inject = ['$resource','BASE_URL_API'];

    function Compte ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'comptes/:id';

        return $resource(resourceUrl, {}, {
            
            'query': { method: 'GET', isArray: true},
            'changeEtat': { method: 'GET',url:BASE_URL_API+'comptes/changeEtat/:id', isArray: false},
            'changeStatut': { method: 'GET',url:BASE_URL_API+'comptes/changeStatut/:id', isArray: false},
            'changeStatutCompteBanque': { method: 'GET',url:BASE_URL_API+'comptes-banque/changeStatut/:id', isArray: false},
             'changeStatutCarte': { method: 'GET',url:BASE_URL_API+'comptes-banque/changeStatut/:id', isArray: false},
            'queryByValeurCompte': { method: 'GET', url:BASE_URL_API+'comptes/:valeurCompte/autres',isArray: false},
            'queryLibelle': {method: 'GET', url: 'api/', isArray: true},
            'queryByTel': {method: 'GET', url: 'api/comptes-by-client-tel/:numeroTel', isArray: true},
            'addCompteComo': {method: 'GET', url: 'api/add-compte-como', isArray: false},
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

    function ValeurCompte ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'valeurComptes';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            }
        });
    }

    function CompteByUO ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'comptesByUO/:uoId';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            }
        });
    }
})();
