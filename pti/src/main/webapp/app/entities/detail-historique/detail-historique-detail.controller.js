(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailHistoriqueDetailController', DetailHistoriqueDetailController);

    DetailHistoriqueDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'DetailHistorique', 'Historique'];

    function DetailHistoriqueDetailController($scope, $rootScope, $stateParams, previousState, entity, DetailHistorique, Historique) {
        var vm = this;

        vm.detailHistorique = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:detailHistoriqueUpdate', function(event, result) {
            vm.detailHistorique = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
