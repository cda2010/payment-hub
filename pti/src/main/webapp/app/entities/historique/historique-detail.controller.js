(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HistoriqueDetailController', HistoriqueDetailController);

    HistoriqueDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Historique'];

    function HistoriqueDetailController($scope, $rootScope, $stateParams, previousState, entity, Historique) {
        var vm = this;

        vm.historique = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:historiqueUpdate', function(event, result) {
            vm.historique = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
