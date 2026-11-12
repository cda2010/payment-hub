(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneReglement', LigneReglement);

    LigneReglement.$inject = ['$resource'];

    function LigneReglement ($resource) {
        var resourceUrl =  'api/ligne-reglements/:id';

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
            },
            'update': { method:'PUT' }
        });
    }
})();
