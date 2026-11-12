(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('GroupeProfilActeur', GroupeProfilActeur);

    GroupeProfilActeur.$inject = ['$resource','BASE_URL_API'];

    function GroupeProfilActeur ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'groupe-profil-acteurs/:id';
        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByType': { method: 'GET',url:BASE_URL_API+'groupe-profil-acteurs/by-type/:acteur', isArray: true},
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
