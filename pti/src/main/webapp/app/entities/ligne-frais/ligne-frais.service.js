(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneFrais', LigneFrais)
        .factory('LigneFraisByTableauFrais', LigneFraisByTableauFrais);

    LigneFrais.$inject = ['$resource','BASE_URL_API'];
    LigneFraisByTableauFrais.$inject = ['$resource','BASE_URL_API'];

    function LigneFrais($resource,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'ligne-frais/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET',isArray: true },
            'get': {
                method: 'GET',
                transformResponse: function(data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method: 'PUT' }
        });
    }

    function LigneFraisByTableauFrais($resource,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'ligne-frais/:id/tableauFrais';
        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true },
            'get': {
                method: 'GET',
                transformResponse: function(data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method: 'PUT' }
        });
    }
})();
