(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('LigneOperation', LigneOperation);

    LigneOperation.$inject = ['$resource'];

    function LigneOperation ($resource) {
        var resourceUrl =  'api/ligne-operations/:id';

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
