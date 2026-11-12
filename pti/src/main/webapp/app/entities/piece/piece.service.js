(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Piece', Piece)
        .factory('PieceByTitulaireId',PieceByTitulaireId)
        .factory('PieceOfTitulaireId',PieceOfTitulaireId);

    Piece.$inject = ['$resource', 'DateUtils','BASE_URL_API'];
    PieceByTitulaireId.$inject = ['$resource', 'DateUtils','BASE_URL_API'];
    PieceOfTitulaireId.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function Piece ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'pieces/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateDelivrance = DateUtils.convertLocalDateFromServer(data.dateDelivrance);
                        data.dateExpiration = DateUtils.convertLocalDateFromServer(data.dateExpiration);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateDelivrance = DateUtils.convertLocalDateToServer(copy.dateDelivrance);
                    copy.dateExpiration = DateUtils.convertLocalDateToServer(copy.dateExpiration);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.dateDelivrance = DateUtils.convertLocalDateToServer(copy.dateDelivrance);
                    copy.dateExpiration = DateUtils.convertLocalDateToServer(copy.dateExpiration);
                    return angular.toJson(copy);
                }
            }
        });
    }

    function PieceByTitulaireId ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'piecesByTitulaireId/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByTitulaireId':{method: 'GET',url: BASE_URL_API+'piecesByTitulaireIds_new', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateDelivrance = DateUtils.convertLocalDateFromServer(data.dateDelivrance);
                        data.dateExpiration = DateUtils.convertLocalDateFromServer(data.dateExpiration);
                    }
                    return data;
                }
            }
        });
    }


    function PieceOfTitulaireId ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'piecesOfTitulaireId/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateDelivrance = DateUtils.convertLocalDateFromServer(data.dateDelivrance);
                        data.dateExpiration = DateUtils.convertLocalDateFromServer(data.dateExpiration);
                    }
                    return data;
                }
            }
        });
    }
})();
