(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Webservice', Webservice);

    Webservice.$inject = ['$resource'];

    function Webservice ($resource) {
        var resourceUrl =  'api/webservices/:id';

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
