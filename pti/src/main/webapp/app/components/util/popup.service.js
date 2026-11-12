(function () {
    "use strict";
    angular.module('ptiApp').factory('PopupService', ['Notification', '_', '$uibModal', PopupService]);
    function PopupService(Notification, _, $uibModal) {
        console.log('***Création du service popup');
        var service = {
            open: open,
            choisir: choisir
        };
        return service;
        function open(data) {
            var vm = data.vm,
                dialogData = data.dialogData,
                datas = data.datas,
                scope = data.scope;
            vm.addItem = addItem;
            vm.editItem = editItem;
            vm.deleteItem = deleteItem;
            if (angular.isUndefined(vm.datas)) {
                vm.datas = [];
            }
            // vm.deleteItem=deleteItem;isUndefined


            /**
             * Ajout d'un élément
             *
             */

            function addItem() {
                $uibModal.open(dialogData.addData).result.then(function (result) {
                    Notification.success('Enregistrement reussit !');
                    /*Ajout de l'élément ajouté dans le tableau, en le triant avec les paramètres courants*/
                    if (datas !== null) {
                        vm.datas.push(result);
                        scope.$emit('PopupService:resultData', { 'result': result });
                        //datas = _.sortBy(vm.datas, vm.predicate, !vm.reverse);
                    } else {
                        console.error("***vm.datas is null");
                    }
                }, function () { });
            }


            /**
             *Modification d'un élément
             */
            function editItem($event, item) {
                $event.stopPropagation();
                dialogData.editData.resolve.entity = angular.copy(item);
                $uibModal.open(dialogData.editData).result.then(function (result) {
                    Notification.success('Modification reussit !');
                    /*Les valeurs dans result écrase celle d'item */
                    angular.forEach(item, function (value, key) {
                        item[key] = result[key];
                    });
                }, function () {

                });
            }

            function deleteItem(index) {
                vm.datas.splice(index, 1);
                Notification.success('Supression reussit !');
            }

        }

        function choisir(datas) {
            var Entity = datas.entity;
            var scope = datas.scope;
            var data = datas.popupDatas;
            var vm = data.vm,
                datas = data.datas
            console.log("choisir agent popup");
            $uibModal.open(data).result.then(function (data) {
                //   var id_entity=data.value[0];
                scope.$emit('ptiApp:valueChoisi', { 'result': true, 'data': data.value, 'entity': Entity });
                /* if(id_entity!=null){
                   console.log("result of popup service");
                      Entity.get({id:id_entity}, function(data) {
                          console.log(data); 
                      scope.$emit('microfinApp:valueChoisi', {'result':true,'data':data,'entity':Entity});                
                     });
                 }*/
            }, function () {
                console.log("erreur serveur");
            });
        }
    }
})();
