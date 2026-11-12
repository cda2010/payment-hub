(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Methode', Methode);

    Methode.$inject = ['$resource'];

    function Methode ($resource) {
        var resourceUrl =  'api/methodes/:id';

        return $resource(resourceUrl, {}, {
            'queryFse': { method: 'GET', url: 'api/methodes/fse/:idfse' ,isArray: true},
            'queryApi': { method: 'GET', url: 'api/methodes/api/:idApi' ,isArray: true},
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
