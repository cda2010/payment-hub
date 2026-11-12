(function () {
    "use strict";
    angular.module('ptiApp').factory('ChoisirUo', ['$uibModal', ChoisirUo]);

    function ChoisirUo($uibModal) {

        var service = {
            open: open,
        };

        return service;


        function open(scope,datas) {
            var paysId;
            var fseId;
            var typeActeurs=[];

            if(angular.isDefined(datas)){//Les informations par default
                console.log("Information...");
                console.log(datas);
                if(angular.isDefined(datas.paysId)) paysId=datas.paysId;
                if(angular.isDefined(datas.fseId)) fseId=datas.fseId;
                if(angular.isDefined(datas.acteurs)) typeActeurs=datas.acteurs;
            }
            $uibModal.open({
                templateUrl: 'app/components/choisir_uo/choisir-uo-dialog.html',
                controller: 'ChoisirUoController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function(){
                        return datas;
                    } 
                }
            }).result.then(function (result) {
                console.log("uo choisi");
                //modalInstance.dismiss("cancel");
                scope.$emit('ptiApp:choisirUo', result.organisation);
            }, function () {
            });
        }
    }
})();