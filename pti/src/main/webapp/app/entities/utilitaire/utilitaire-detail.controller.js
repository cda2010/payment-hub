(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UtilitaireDetailController', UtilitaireDetailController);

    UtilitaireDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Utilitaire'];

    function UtilitaireDetailController($scope, $rootScope, $stateParams, previousState, entity, Utilitaire) {
        var vm = this;

        vm.utilitaire = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:utilitaireUpdate', function(event, result) {
            vm.utilitaire = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
