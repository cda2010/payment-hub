(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ProfilActeur', ProfilActeur);

    ProfilActeur.$inject = ['$resource','BASE_URL_API'];

    function ProfilActeur ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'profil-acteurs/:id';

        return $resource(resourceUrl, {}, {
            'queryProfileClient': { method: 'GET',url:'api/profil-acteurs/acteur/:acteur', isArray: true},
            'query': { method: 'GET', isArray: true},
            'findOne': { method: 'GET',url:BASE_URL_API+'profil-acteurs/:id', isArray: false},
            'queryByGroupe': { method: 'GET',url:BASE_URL_API+'profil-acteurs/by-groupe/:groupeId', isArray: true},
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
})();
