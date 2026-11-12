(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('AutreParametre', AutreParametre);

    AutreParametre.$inject = ['$resource'];

    function AutreParametre ($resource) {
        var resourceUrl =  'api/autre-parametres/:id';

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
