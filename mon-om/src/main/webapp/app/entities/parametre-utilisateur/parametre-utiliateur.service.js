(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('ParametreUtiliateur', ParametreUtiliateur);

    ParametreUtiliateur.$inject = ['$resource'];

    function ParametreUtiliateur ($resource) {
        var resourceUrl =  'api/parametre-utiliateurs/:id';

        return $resource(resourceUrl, {}, {
            'queryInterface': { method: 'GET', url: 'api/parametre-utiliateurs/interface/:idInterface', isArray: true},
            'queryService': { method: 'GET', url: 'api/parametre-utiliateurs/service/:idservice', isArray: true},
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
