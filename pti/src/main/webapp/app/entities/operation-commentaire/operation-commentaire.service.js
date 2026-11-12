(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('OperationCommentaire', OperationCommentaire);

    OperationCommentaire.$inject = ['$resource'];

    function OperationCommentaire ($resource) {
        var resourceUrl =  'api/operation-commentaires/:id';

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
